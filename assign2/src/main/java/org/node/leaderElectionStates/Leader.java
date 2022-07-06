package org.node.leaderElectionStates;

import org.node.Node;

public class Leader implements LeaderElectionState {
    private int messagesSent;

    public Leader(){
        this.messagesSent = 0;
    }

    @Override
    public void execute(Node node) {
//        System.out.println("Leader exe " + messagesSent + " pri " + node.getPriority(node.getNodeInfo().getHashValue()));

        if( !node.sendMembershipMessage() ){
            System.err.println("Could not send global membership message");
        }
        messagesSent++;
        if(messagesSent == 20){
            node.setLeaderElectionState(new Follower());
        }
    }

    @Override
    public int getWaitTime() {
        return 1500;
    }

}
