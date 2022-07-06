package org.node.jobs;

import org.node.Node;

public class CloseStoreJob implements Runnable {
    private final Node node;

    public CloseStoreJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        node.getNodeStore().reallocateCurrFiles();
    }
}
