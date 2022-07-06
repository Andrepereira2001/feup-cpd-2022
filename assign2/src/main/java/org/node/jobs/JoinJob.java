package org.node.jobs;

import org.node.Node;
import org.utils.Utils;

public class JoinJob implements Runnable {
    private final Node node;

    public JoinJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        node.reset();
        node.setShutdown(false);

        String address = this.node.getNodeInfo().getAddress();
        int membershipCounter = node.getNodeInfo().getNodeCounter();

        System.out.println("JOIN | " + node.getNodeInfo() + " | " + membershipCounter);

        if (membershipCounter % 2 != 0) {
            System.err.println("JOIN ERROR | Invalid membership counter: " + node.getNodeInfo());
            return;
        }

        node.setupUnicast();
        node.setupMulticast();
        node.setUnicastHandler();
        node.getNodeInfo().addMembershipLog(address, membershipCounter);
        node.addMember(address);

        while (node.getJoinMessagesSent() < 3 && node.getMembershipMessagesReceived() < 3) {
            if (!node.sendJoinMessage()) {
                System.err.println("JOIN ERROR | Error message: " + node.getNodeInfo());
                return;
            }
            System.out.println("Join message loop");
            Utils.wait(1000);
        }

        node.createStore();
        node.getNodeInfo().setNodeCounter(membershipCounter + 1);
        node.setMulticastHandler();
        node.startConsistencyJob();
    }
}
