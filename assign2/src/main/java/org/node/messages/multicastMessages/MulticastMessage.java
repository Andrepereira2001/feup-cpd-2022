package org.node.messages.multicastMessages;

import org.node.Node;
import org.node.messages.Message;
import org.node.messages.NullMessage;
import org.node.messages.MembershipMessage;

import java.net.InetAddress;
import java.util.List;

public abstract class MulticastMessage extends Message {
    private final InetAddress senderIP;
    private final int senderPort;

    public MulticastMessage(Node node, InetAddress senderIP, int senderPort) {
        super(node);
        this.senderIP = senderIP;
        this.senderPort = senderPort;
    }

    public InetAddress getSenderIP() {
        return senderIP;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public static Message parseMessage(Node node, String message) {
        List<String> data = List.of(message.split(";"));
        List<String> sender = List.of(data.get(1).split(":"));
        String messageType = "";
        InetAddress senderIP = null;
        int senderPort = 0;

        try {
            senderIP = InetAddress.getByName(sender.get(0));
            senderPort = Integer.parseInt(sender.get(1));
            messageType = node.getNodeInfo().isEqual(senderIP, senderPort) ? "" : data.get(0);
        } catch (Exception e) {
            System.err.println("Error parsing sender IP address: " + e);
        }

        return switch (messageType) {
            case "MEMBERSHIP" -> new MembershipMessage(node, data);
            case "LEADER" -> new LeaderMessage(node, senderIP, senderPort, data);
            case "JOIN" -> new JoinMessage(node, senderIP, senderPort, data);
            case "LEAVE" -> new LeaveMessage(node, senderIP, senderPort, data);
            case "SYNC" -> new SyncMessage(node, senderIP, senderPort, data);
            case "ELECTION" -> new ElectionMessage(node, senderIP, senderPort, data);
            default -> new NullMessage();
        };
    }
}
