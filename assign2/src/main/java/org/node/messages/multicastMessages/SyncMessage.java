package org.node.messages.multicastMessages;

import org.node.Node;
import org.utils.Utils;

import java.net.InetAddress;
import java.util.List;

public class SyncMessage extends MulticastMessage {
    private final int counter;

    public SyncMessage(Node node, InetAddress senderIP, int senderPort, List<String> data) {
        super(node, senderIP, senderPort);
        this.counter = Integer.parseInt(data.get(2));
    }

    public static String build(InetAddress nodeIP, int nodePort, int counter) {
        return "SYNC;" + nodeIP.getHostAddress() + ":" + nodePort + ";" + counter;
    }

    @Override
    public void run() {
        Utils.waitRandom(200);
        this.node.sendMembershipMessage(this.getSenderIP(), this.getSenderPort());
        this.node.getNodeStore().sendFilesToNewNode(this.getSenderIP(), this.getSenderPort());
    }
}
