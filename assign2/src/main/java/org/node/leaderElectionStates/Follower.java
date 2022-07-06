package org.node.leaderElectionStates;

import org.node.Node;

public class Follower implements LeaderElectionState{
    int lastMembershipMessages;

    public Follower(){
        this.lastMembershipMessages = -1;
    }

    @Override
    public void execute(Node node) {
        int currMembershipMessage = node.getMembershipMessagesReceived();
//        System.out.println("Follow - currMem: " + currMembershipMessage + "  lastMem: " + lastMembershipMessages + " pri: " +
//                node.getPriority(node.getNodeInfo().getHashValue()));

        if(currMembershipMessage == lastMembershipMessages){
            node.setLeaderElectionState(new Candidate(node));
        }

        this.lastMembershipMessages = currMembershipMessage;
    }

    @Override
    public int getWaitTime() {
        return 1800;
    }

}
