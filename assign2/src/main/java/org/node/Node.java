package org.node;

import org.node.leaderElectionStates.Follower;
import org.node.leaderElectionStates.LeaderElectionState;
import org.node.leaderElectionStates.PriorityMember;
import org.node.messages.MembershipMessage;
import org.node.messages.unicastMessages.AliveMessage;
import org.node.jobs.*;
import org.node.messages.multicastMessages.*;
import org.utils.FileUtils;
import org.utils.MulticastUtils;
import org.utils.UnicastUtils;
import org.utils.Utils;
import org.services.MembershipService;

import java.io.File;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Node implements MembershipService {
    private Thread multicastThread, unicastThread, consistencyThread;
    private NetworkInterface multicastInterface;
    protected DatagramSocket multicastSocket;
    protected ServerSocket unicastSocket;
    protected NodeInfo nodeInfo;
    protected NodeStore nodeStore;
    private int joinMessagesSent, membershipMessagesReceived;
    private boolean shutdown;
    private LeaderElectionState leaderElectionState;
    private final Executor service;
    private final List<PriorityMember> priorityMembers = new ArrayList<>();

    public Node(String mcastAddr, int mcastPort, String nodeId, int nodePort) throws RemoteException {
        try {
            nodeInfo = new NodeInfo(
                    InetAddress.getByName(mcastAddr),
                    mcastPort,
                    InetAddress.getByName(nodeId),
                    nodePort
            );
            nodeStore = new NodeStore(this);
        } catch (Exception e) {
            System.err.println("Invalid Address " + e);
        }

        service = Executors.newCachedThreadPool();

        if (nodeInfo.getNodeCounter() % 2 != 0) {
            // node was already in the cluster - must sync
            sync();
        }
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public NodeStore getNodeStore() {
        return nodeStore;
    }

    public DatagramSocket getMulticastSocket() {
        return multicastSocket;
    }

    public ServerSocket getUnicastSocket() {
        return unicastSocket;
    }

    public int getJoinMessagesSent() {
        return joinMessagesSent;
    }

    public int getMembershipMessagesReceived() {
        return membershipMessagesReceived;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public LeaderElectionState getLeaderElectionState() {
        return leaderElectionState;
    }

    public void setLeaderElectionState(LeaderElectionState leaderElectionState) {
        this.leaderElectionState = leaderElectionState;
    }

    public void incrementMembershipMessages() {
        membershipMessagesReceived++;
    }

    public void setupMulticast() {
        if (multicastSocket == null) {
            multicastInterface = MulticastUtils.createNetworkInterface("");
            multicastSocket = MulticastUtils.createDatagram(
                    nodeInfo.getMcastAddr(),
                    nodeInfo.getMcastPort(),
                    multicastInterface);
        }
    }

    public void setupUnicast() {
        if (unicastSocket == null) {
            unicastSocket = UnicastUtils.createSocket(nodeInfo.getNodePort());
        }
    }

    @Override
    public void join() throws RemoteException {
        service.execute(new JoinJob(this));
    }

    @Override
    public void leave() throws RemoteException {
        service.execute(new LeaveJob(this));
    }

    public void sync() throws RemoteException {
        joinMessagesSent = 0;
        membershipMessagesReceived = 0;

        service.execute(new SyncJob(this));
    }

    public void closeStore() {
        this.getNodeStore().reallocateCurrFiles();
        //service.execute(new CloseStoreJob(this));
    }

    public void createStore() {
        service.execute(new InitializeStoreJob(this));
    }

    public void setUnicastHandler() {
        UnicastExecutor unicastExecutor = new UnicastExecutor(this);
        unicastThread = new Thread(unicastExecutor);
        unicastThread.start();
    }

    public void setMulticastHandler() {
        MulticastExecutor multicastExecutor = new MulticastExecutor(this);
        multicastThread = new Thread(multicastExecutor);
        multicastThread.start();
    }

    public void startConsistencyJob() {
        this.setLeaderElectionState(new Follower());
        ConsistencyJob consistencyJob = new ConsistencyJob(this);
        consistencyThread = new Thread(consistencyJob);
        consistencyThread.start();
    }

    public boolean sendJoinMessage() {
        joinMessagesSent++;
        return MulticastUtils.sendMessage(
                multicastSocket,
                JoinMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getNodeCounter(),
                        getJoinMessagesSent()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort());
    }

    public boolean sendLeaveMessage() {
        return MulticastUtils.sendMessage(
                multicastSocket,
                LeaveMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getNodeCounter()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort());
    }

    public boolean sendSyncMessage() {
        return MulticastUtils.sendMessage(
                multicastSocket,
                SyncMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getNodeCounter()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort());
    }

    public boolean sendMembershipMessage(InetAddress destinationIP, int destinationPort) {
        Socket joiningNode = UnicastUtils.connectSocket(destinationIP, destinationPort);
        return UnicastUtils.sendMessage(
                joiningNode,
                MembershipMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getClusterMembers(),
                        nodeInfo.getMembershipLogs()));
    }

    public boolean sendMembershipMessage() {
        return MulticastUtils.sendMessage(
                multicastSocket,
                MembershipMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getClusterMembers(),
                        nodeInfo.getMembershipLogs()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort());
    }

    public boolean sendElectionMessage(){
        return MulticastUtils.sendMessage(
                multicastSocket,
                ElectionMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getNodeCountersSum()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort());
    }

    public boolean sendLeaderMessage() {
        return MulticastUtils.sendMessage(
                multicastSocket,
                LeaderMessage.build(
                        nodeInfo.getNodeIP(),
                        nodeInfo.getNodePort(),
                        nodeInfo.getNodeCountersSum()),
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort()
        );
    }

    public boolean sendRedirectMessage(Socket socket, List<String> addresses){
        return UnicastUtils.sendMessage(
                socket,
                "REDIRECT;" + String.join(",",addresses)
        );
    }

    public void shutdown() {
        setShutdown(true);

        MulticastUtils.closeDatagram(
                multicastSocket,
                nodeInfo.getMcastAddr(),
                nodeInfo.getMcastPort(),
                multicastInterface);

        if (unicastSocket != null) {
            UnicastUtils.closeServerSocket(unicastSocket);
        }

        multicastSocket = null;
        multicastInterface = null;
        unicastSocket = null;
    }

    public void execute(Runnable command) {
        service.execute(command);
    }

    public void addToPriorityMembersList(PriorityMember priorityMember){
        synchronized (priorityMembers) {
            if (!priorityMembers.contains(priorityMember)) {
                priorityMembers.add(priorityMember);
                Collections.sort(priorityMembers);
            }
        }
    }

    public void removeFromPriorityMembersList(String hash){
        synchronized (priorityMembers) {
            for (int i = 0; i < priorityMembers.size(); i++) {
                if (priorityMembers.get(i).getHash().equals(hash)) {
                    priorityMembers.remove(i);
                    break;
                }
            }
        }
    }

    public Socket sendAliveMessage(InetAddress destinationIP, int destinationPort) {
        Socket joiningNode = UnicastUtils.connectSocket(destinationIP, destinationPort);
        if(!UnicastUtils.sendMessage(joiningNode, AliveMessage.build(
                nodeInfo.getNodeIP(), nodeInfo.getNodePort())
        )){
            joiningNode =  null;
        }
        return joiningNode;
    }

    public void reset() {
        File membershipLog = nodeInfo.getMembershipFile();
        FileUtils.writeToFile(membershipLog, "", false);
        File clusterMembersLog = nodeInfo.getClusterMembersFile();
        FileUtils.writeToFile(clusterMembersLog, "", false);

        joinMessagesSent = 0;
        membershipMessagesReceived = 0;
    }

    public void addMember(String address) {
        if(!this.nodeInfo.isClusterMember(address)) {
            this.getNodeStore().addClusterMember(address);
        }
        PriorityMember pm = new PriorityMember(Utils.sha256(address), this.nodeInfo.getNodeCountersSum());
        this.addToPriorityMembersList(pm);
    }

    public void removeMember(String address) {
        this.getNodeStore().removeClusterMember(address);
        this.removeFromPriorityMembersList(Utils.sha256(address));
    }

    public int getPriority(String hash){
//        System.out.println("members " + priorityMembers);
        synchronized (priorityMembers) {
            for (int i = 0; i < priorityMembers.size(); i++) {
                if (priorityMembers.get(i).getHash().equals(hash)) {
                    return i;
                }
            }
        }
        return 100;
    }

    public void updateCounter(String hash, int counter){
        this.removeFromPriorityMembersList(hash);
        this.addToPriorityMembersList(new PriorityMember(hash, counter));
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: java Store <IP_mcast_addr> <IP_mcast_port> <node_id>  <Store_port>");
            return;
        }

        try {
            String mcastAddr = args[0];
            int mcastPort = Integer.parseInt(args[1]);
            String nodeId = args[2]; //IP do Node 127._._._
            int nodePort = Integer.parseInt(args[3]); //porta de acesso ao node

            //vou ter que gerar o ID dele para o meter no anel
            Node nodeObject = new Node(mcastAddr, mcastPort, nodeId, nodePort);
            MembershipService stub = (MembershipService) UnicastRemoteObject.exportObject(nodeObject, 0);
            // Bind the remote object's stub in the registry
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(nodePort + 300);
                System.out.println("Creating registry");
            } catch (Exception E) {
                registry = LocateRegistry.getRegistry(nodePort);
                System.out.println("Getting registry");
            }
            registry.rebind(nodeId + nodePort, stub);

            System.out.println("Node ready!");

        } catch (Exception e) {
            System.err.println("Node exception: " + e);
            e.printStackTrace();
        }
    }
}
