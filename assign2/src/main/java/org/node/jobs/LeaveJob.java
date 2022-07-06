package org.node.jobs;

import org.node.Node;

public class LeaveJob implements Runnable {
    Node node;

    public LeaveJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        String address = this.node.getNodeInfo().getAddress();
        int membershipCounter = node.getNodeInfo().getNodeCounter();

        System.out.println("LEAVE | " + node.getNodeInfo() + " | " + membershipCounter);

        if (membershipCounter % 2 != 1) {
            System.err.println("LEAVE ERROR | Invalid membership counter: " + node.getNodeInfo());
            return;
        }

        if (!node.sendLeaveMessage()) {
            System.err.println("LEAVE ERROR | Error message: " + node.getNodeInfo());
            return;
        }

        node.removeMember(address);
        node.closeStore();
        node.shutdown();
        node.getNodeInfo().setNodeCounter(membershipCounter + 1);
        node.getNodeInfo().addMembershipLog(address, membershipCounter);
    }
}
