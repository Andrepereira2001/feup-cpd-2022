package org.node.messages.unicastMessages;

import org.node.Node;
import org.utils.UnicastUtils;

import java.net.InetAddress;
import java.net.Socket;

public class AliveMessage extends UnicastMessage {

    public AliveMessage(Node node, Socket socket) {
        super(node, socket);
    }

    public static String build(InetAddress nodeID, int nodePort) {
        return "ALIVE;" + nodeID.getHostAddress() + ":" + nodePort;
    }

    @Override
    public void run() {
        UnicastUtils.sendMessage(this.getSocket(), "OK;");
    }
}
