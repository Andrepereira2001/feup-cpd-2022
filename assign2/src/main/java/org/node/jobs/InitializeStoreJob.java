package org.node.jobs;

import org.node.Node;

import java.util.List;

public class InitializeStoreJob implements Runnable {
    private final Node node;

    public InitializeStoreJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        List<String> members = node.getNodeInfo().getClusterMembers();
        node.getNodeStore().initializeStore(members);
    }
}
