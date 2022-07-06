package org.node.messages.unicastMessages;

import org.node.Node;
import org.utils.UnicastUtils;

import java.net.Socket;
import java.util.List;

public class GetMessage extends UnicastMessage {
    private final String hash;

    public GetMessage(Node node, Socket socket, List<String> data) {
        super(node, socket);
        this.hash = data.get(1).strip();
    }

    public static String build(String hash) {
        return "GET;" + hash;
    }

    @Override
    public void run() {
        String storingNode = node.getNodeStore().getStoringNode(this.hash);
        List<String> replicationNodes = node.getNodeStore().getReplicationNodes(storingNode);
        String data;
        if (replicationNodes.contains(node.getNodeInfo().getAddress()) && node.getNodeStore().existData(hash)) {
            //Send OK message
            UnicastUtils.sendMessage(this.getSocket(), "OK;");
            //Receive data
            data = node.getNodeStore().get(hash);

            UnicastUtils.sendMessage(this.getSocket(), data);

        } else {
            node.sendRedirectMessage(this.getSocket(), replicationNodes);
        }
    }
}
