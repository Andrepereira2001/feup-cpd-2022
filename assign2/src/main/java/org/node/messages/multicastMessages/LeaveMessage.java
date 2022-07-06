package org.node.messages.multicastMessages;

import org.node.Node;

import java.net.InetAddress;
import java.util.List;

public class LeaveMessage extends MulticastMessage {
    private final int counter;

    public LeaveMessage(Node node, InetAddress senderIP, int senderPort, List<String> data) {
        super(node, senderIP, senderPort);
        this.counter = Integer.parseInt(data.get(2));
    }

    public static String build(InetAddress nodeIP, int nodePort, int counter) {
        return "LEAVE;" + nodeIP.getHostAddress() + ":" + nodePort + ";" + counter;
    }

    @Override
    public void run() {
        String address = this.getSenderIP().getHostAddress() + ":" + this.getSenderPort();
        this.node.getNodeInfo().addMembershipLog(address, this.counter);
        node.removeMember(address);
    }

}
