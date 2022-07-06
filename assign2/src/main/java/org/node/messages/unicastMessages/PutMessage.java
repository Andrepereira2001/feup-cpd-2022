package org.node.messages.unicastMessages;

import org.node.Node;
import org.utils.UnicastUtils;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class PutMessage extends UnicastMessage {
    private final String hash;

    public PutMessage(Node node, Socket socket, List<String> data) {
        super(node, socket);
        this.hash = data.get(1).strip();
    }

    public static String build(String hash) {
        return "PUT;" + hash;
    }

    @Override
    public void run() {
        String storingNode = node.getNodeStore().getStoringNode(this.hash);
        List<String> replicationNodes = node.getNodeStore().getReplicationNodes(storingNode);
        if (replicationNodes.contains(node.getNodeInfo().getAddress())) {

            if(node.getNodeStore().existData(this.hash)){
                this.dismissData();
            } else{
                this.receiveData(replicationNodes);
            }

        } else {
            node.sendRedirectMessage(this.getSocket(),replicationNodes);
        }
    }

    private void receiveData(List<String> replicationNodes){
        String data;
        //Send OK message
        UnicastUtils.sendMessage(this.getSocket(), "OK;");
        //Receive data
        //TODO may receive multiple data receive data with while
        data = UnicastUtils.receiveMessage(this.getSocket());
        UnicastUtils.closeConnectionSocket(this.getSocket());

        node.getNodeStore().store(hash,data);

        this.replicateData(replicationNodes, data);
    }

    private void dismissData(){
        UnicastUtils.sendMessage(this.getSocket(), "EXIST;");
    }

    private void replicateData(List<String> repNodes, String data) {
        for (String node : repNodes){
            if(!Objects.equals(node,this.node.getNodeInfo().getAddress())) {
                InetAddress ip = null;
                int port = 0;
                Socket socket;
                String response;

                try {
                    List<String> nodeInfo = List.of(node.split(":"));
                    ip = InetAddress.getByName(nodeInfo.get(0));
                    port = Integer.parseInt(nodeInfo.get(1));

                }
                catch (Exception e){
                    System.err.println("Address parsing error in replication " + e);
                }

                socket = UnicastUtils.connectSocket(ip,port);
                if(socket == null) continue;

                UnicastUtils.sendMessage(socket,PutMessage.build(hash));
                response = UnicastUtils.receiveMessage(socket).strip();
                if (response.contains("OK;")){
                    UnicastUtils.sendMessage(socket, data);
                } else {
                    UnicastUtils.closeConnectionSocket(socket);
                }

            }
        }
    }
}
