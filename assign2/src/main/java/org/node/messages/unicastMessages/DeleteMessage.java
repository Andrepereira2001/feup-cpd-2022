package org.node.messages.unicastMessages;

import org.node.Node;
import org.utils.UnicastUtils;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class DeleteMessage extends UnicastMessage {
    private final String hash;

    public DeleteMessage(Node node, Socket socket, List<String> data) {
        super(node, socket);
        this.hash = data.get(1).strip();
    }

    public static String build(String hash) {
        return "DELETE;" + hash;
    }

    @Override
    public void run() {
        String address = node.getNodeStore().getStoringNode(this.hash);
        List<String> replicationNodes = node.getNodeStore().getReplicationNodes(address);

        if (replicationNodes.contains(node.getNodeInfo().getAddress())) {

            if(node.getNodeStore().existData(this.hash)){
                this.receiveDelete(replicationNodes);
            } else{
                this.dismissDelete();
            }

        } else {
            node.sendRedirectMessage(this.getSocket(), replicationNodes);
        }
    }

    private void dismissDelete() {
        UnicastUtils.sendMessage(this.getSocket(), "INVALID;");
    }

    private void receiveDelete(List<String> replicationNodes) {
        //Send OK message
        UnicastUtils.sendMessage(this.getSocket(), "OK;");

        if (node.getNodeStore().delete(hash)) {
            replicateDelete(replicationNodes);
        }
    }

    private void replicateDelete(List<String> repNodes) {
        for (String node : repNodes) {
            if (!Objects.equals(node, this.node.getNodeInfo().getAddress())) {
                InetAddress ip = null;
                int port = 0;
                Socket socket;

                try {
                    List<String> nodeInfo = List.of(node.split(":"));
                    ip = InetAddress.getByName(nodeInfo.get(0));
                    port = Integer.parseInt(nodeInfo.get(1));

                } catch (Exception e) {
                    System.err.println("Address parsing error in replication " + e);
                }

                socket = UnicastUtils.connectSocket(ip, port);
                if (socket == null) continue;

                UnicastUtils.sendMessage(socket, DeleteMessage.build(hash));
                UnicastUtils.receiveMessage(socket);
                UnicastUtils.closeConnectionSocket(socket);

            }
        }
    }
}
