package org.node.messages.unicastMessages;

import org.node.Node;
import org.node.messages.MembershipMessage;
import org.node.messages.Message;
import org.node.messages.NullMessage;

import java.net.Socket;
import java.util.List;

public abstract class UnicastMessage extends Message {
    private final Socket socket;

    public UnicastMessage(Node node, Socket socket) {
        super(node);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public static Message parseMessage(Node node, Socket socket, String message) {
        List<String> data = List.of(message.strip().split(";"));
        String messageType = data.get(0);

        return switch (messageType) {
            case "MEMBERSHIP" -> new MembershipMessage(node, data);
            case "GET" -> new GetMessage(node, socket, data);
            case "PUT" -> new PutMessage(node, socket, data);
            case "DELETE" -> new DeleteMessage(node, socket, data);
            case "REPLICATE" -> new ReplicationMessage(node, socket, data);
            case "ALIVE" -> new AliveMessage(node,socket);
            default -> new NullMessage();
        };
    }
}
