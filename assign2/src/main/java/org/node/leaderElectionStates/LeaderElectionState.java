package org.node.leaderElectionStates;

import org.node.Node;

public interface LeaderElectionState {

    public void execute(Node node);

    public int getWaitTime();
}
