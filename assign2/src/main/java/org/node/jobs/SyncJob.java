package org.node.jobs;

import org.node.Node;

public class SyncJob implements Runnable {
    private final Node node;

    public SyncJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        node.setShutdown(false);

        String address = this.node.getNodeInfo().getAddress();
        int membershipCounter = node.getNodeInfo().getNodeCounter() - 1;

        System.out.println("SYNC | " + node.getNodeInfo() + " | " + membershipCounter);

        node.setupUnicast();
        node.setupMulticast();
        node.setUnicastHandler();
        node.getNodeInfo().addMembershipLog(address, membershipCounter);
        node.addMember(address);

        if (!node.sendSyncMessage()) {
            System.err.println("SYNC ERROR | Error message: " + node.getNodeInfo());
            return;
        }

        node.createStore();
        node.setMulticastHandler();
        node.startConsistencyJob();
    }
}
