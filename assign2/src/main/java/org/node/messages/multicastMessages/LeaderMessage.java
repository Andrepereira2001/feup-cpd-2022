package org.node.messages.multicastMessages;

import org.node.Node;
import org.node.leaderElectionStates.Follower;
import org.utils.Utils;

import java.net.InetAddress;
import java.util.List;

public class LeaderMessage extends MulticastMessage {
    private final int senderCountersSum;

    public LeaderMessage(Node node, InetAddress senderIP, int senderPort, List<String> data) {
        super(node, senderIP, senderPort);
        this.senderCountersSum = Integer.parseInt(data.get(2));
    }

    public static String build(InetAddress nodeIP, int nodePort, int counterSum) {
        return "LEADER;" + nodeIP.getHostAddress() + ":" + nodePort + ";" + counterSum;
    }

    @Override
    public void run() {
        String senderHash = Utils.sha256(this.getSenderIP().getHostAddress() + ":" + this.getSenderPort());

        node.updateCounter(senderHash, senderCountersSum);
        if(node.getPriority(node.getNodeInfo().getHashValue()) > node.getPriority(senderHash)){
            node.setLeaderElectionState(new Follower());
        }

    }
}
