package org.node.messages.unicastMessages;

import org.node.Node;
import org.utils.UnicastUtils;

import java.net.Socket;
import java.util.List;

public class ReplicationMessage extends UnicastMessage{
    private final String hash;

    public ReplicationMessage(Node node, Socket socket, List<String> data) {
        super(node, socket);
        this.hash = data.get(1).strip();
    }

    public static String build(String hash) {
        return "REPLICATE;" + hash;
    }

    @Override
    public void run() {

        if(node.getNodeStore().existData(this.hash)){
            UnicastUtils.sendMessage(this.getSocket(), "EXIST;");
        }
        else if(hash.contains("_tombstone")){
            this.handleDeletedHash();
        } else{
            this.handleDataHash();
        }

    }

    private void handleDeletedHash(){
        String dataHash = List.of(hash.split("_")).get(0);

        if(node.getNodeStore().existData(dataHash)){
            node.getNodeStore().delete(dataHash);
        }
        UnicastUtils.sendMessage(this.getSocket(), "CHANGED;");

    }

    private void handleDataHash(){
        String data;

        if(node.getNodeStore().isDeleted(this.hash)){
            UnicastUtils.sendMessage(this.getSocket(), "DELETED;");
        }
        else {
            UnicastUtils.sendMessage(this.getSocket(), "OK;");
            //Receive data
            //TODO may receive multiple data receive data with while
            data = UnicastUtils.receiveMessage(this.getSocket());
            UnicastUtils.closeConnectionSocket(this.getSocket());

            node.getNodeStore().store(hash, data);
        }
    }
}
