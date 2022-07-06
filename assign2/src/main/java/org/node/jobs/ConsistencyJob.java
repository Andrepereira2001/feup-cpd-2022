package org.node.jobs;

import org.node.Node;
import org.node.leaderElectionStates.LeaderElectionState;
import org.utils.Utils;

public class ConsistencyJob implements Runnable {
    Node node;

    public ConsistencyJob(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!node.isShutdown()) {
            LeaderElectionState state = node.getLeaderElectionState();
            state.execute(node);
            Utils.wait(state.getWaitTime());
        }
    }
}
