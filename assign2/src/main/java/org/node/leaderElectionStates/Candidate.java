package org.node.leaderElectionStates;

import org.node.Node;
import org.utils.UnicastUtils;
import org.utils.Utils;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Candidate implements LeaderElectionState{

    Candidate(Node node){
        node.sendElectionMessage();
        node.updateCounter(node.getNodeInfo().getHashValue(),node.getNodeInfo().getNodeCountersSum());
    }

    @Override
    public void execute(Node node) {
//        System.out.println("Candidate " + " pri " + node.getPriority(node.getNodeInfo().getHashValue()));

        becomeLeader(node);
    }

    private void becomeLeader(Node node){

        if(verifyAlive(node)){
            node.setLeaderElectionState(new Follower());
        }
        else {
            node.setLeaderElectionState(new Leader());
            if (!node.sendLeaderMessage()) {
                System.err.println("Could not send global leader message");
            }
        }
    }



    private boolean verifyAlive(Node node){
        int currPriority = node.getPriority(node.getNodeInfo().getHashValue());
        Socket socket;
        InetAddress ip = null;
        int port = 0;

        for(String member: node.getNodeInfo().getClusterMembers()){
            String hash = Utils.sha256(member);
            int priority = node.getPriority(hash);
            if(priority < currPriority){
                try{
                    List<String> addressData = List.of(member.split(":"));
                    ip = InetAddress.getByName(addressData.get(0));
                    port = Integer.parseInt(addressData.get(1));
                }catch (Exception e){
                    System.err.println("Error parsing address Candidate " + e);
                }

                socket = node.sendAliveMessage(ip,port);
                if(socket != null && UnicastUtils.receiveMessage(socket).contains("OK;")){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getWaitTime() {
        return 2800;
    }

}
