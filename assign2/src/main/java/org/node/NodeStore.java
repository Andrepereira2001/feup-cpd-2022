package org.node;

import org.node.messages.unicastMessages.ReplicationMessage;
import org.utils.FileUtils;
import org.utils.UnicastUtils;
import org.utils.Utils;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class NodeStore {
    private final Node node;
    private final TreeMap<String, String> membersMap;
    private final String storePath;

    NodeStore(Node node) {
        this.node = node;
        this.membersMap = new TreeMap<>();
        this.storePath = "store/" + node.getNodeInfo() + "/storage";
        FileUtils.createFolder(this.storePath);
        System.out.println("Created node store with hash " + node.getNodeInfo().getHashValue());
    }

    public void initializeStore(List<String> members) {
        for (String elem : members) {
            this.membersMap.put(Utils.sha256(elem), elem);
        }
    }

    public void addClusterMember(String address) {
        node.getNodeInfo().addClusterMember(address);
        membersMap.put(Utils.sha256(address), address);
    }

    public void removeClusterMember(String address) {
        node.getNodeInfo().removeClusterMember(address);
        membersMap.remove(Utils.sha256(address));
    }

    public String getStoringNode(String hash) {
        List<String> membersHash = new ArrayList<>(this.membersMap.keySet());
        String node = null;
        int searchResult = Collections.binarySearch(membersHash, hash);
        if (searchResult < -1) {
            searchResult = (-searchResult - 1) % membersHash.size();
            node = membersMap.get(membersHash.get(searchResult));

        }
        return node;
    }

    public List<String> getReplicationNodes (String storingNode) {
        List<String> membersHash = new ArrayList<>(this.membersMap.keySet());
        int storingIdx =  membersHash.indexOf(Utils.sha256(storingNode));
        List<String> repNodes = new ArrayList<>();

        for(int i = 0; i < Math.min(3, membersHash.size()); i++){
            storingIdx = (storingIdx + 1) % membersHash.size();
            repNodes.add(membersMap.get(membersHash.get(storingIdx)));
        }

        return repNodes;
    }

    public void store(String key, String value){
        this.removeTombstone(key);

        File file = FileUtils.createFile(this.storePath + "/" + key );
        FileUtils.writeToFile(file,value,false);
    }

    public String get(String key){
        File file = FileUtils.getFile(this.storePath + "/" + key );
        return String.join("\n",FileUtils.readFromFile(file));
    }

    public boolean delete(String key){
        File file = FileUtils.getFile(this.storePath + "/" + key);
        File rename = FileUtils.getFile(this.storePath + "/" + key + "_tombstone");
        if(rename.exists()){
            return file.delete();
        }
        else{
            return file.renameTo(rename);
        }
    }

    public boolean existData(String key){
        File file = FileUtils.getFile(this.storePath + "/" + key );
        return file.exists();
    }

    public void removeTombstone(String key){
        File file = FileUtils.getFile(this.storePath + "/" + key + "_tombstone" );
        if(file.exists()){
            file.delete();
        }
    }

    public boolean isDeleted(String key){
        File file = FileUtils.getFile(this.storePath + "/" + key + "_tombstone" );
        return file.exists();
    }



    public void sendFilesToNewNode(InetAddress senderIP, int senderPort){
        List<File> files = FileUtils.getAllFiles(this.storePath);
        String senderAddress = senderIP.getHostAddress() + ":" + senderPort;
        String response;

        for(File file : files){
            //verify if any of my files is for new node
            String storingNode = getStoringNode(file.getName());
            List<String> repNodes = getReplicationNodes(storingNode);

            if(repNodes.contains(senderAddress)){
                //send file to new node
                Socket nodeSocket = UnicastUtils.connectSocket(senderIP, senderPort);
                if(nodeSocket == null) continue;

                UnicastUtils.sendMessage(nodeSocket, ReplicationMessage.build(file.getName()));
                response = UnicastUtils.receiveMessage(nodeSocket).strip();

                if (response.contains("OK;")) {
                    UnicastUtils.sendMessage(
                            nodeSocket,
                            String.join("\n", FileUtils.readFromFile(file))
                    );
                } else if(response.contains("DELETED;")){
                    node.getNodeStore().delete(file.getName());
                }

                //delete file from curr node if not in repNodes
                if(!repNodes.contains(node.getNodeInfo().getAddress())) {
                    FileUtils.deleteFile(file);
                }
            }
        }
    }

    public void reallocateCurrFiles(){
        List<File> files = FileUtils.getAllFiles(this.storePath);
        String response, storingNode;
        InetAddress ip = null;
        int port = 0;
        List<String> repNodes;

        for(File file : files){
            storingNode = getStoringNode(file.getName());
            repNodes = getReplicationNodes(storingNode);

            for(String node : repNodes) {
                try {
                    List<String> addressData = List.of(node.split(":"));
                    ip = InetAddress.getByName(addressData.get(0));
                    port = Integer.parseInt(addressData.get(1));
                } catch (Exception e) {
                    System.err.println("Error while parsing address reallocate file " + e);
                }

                //send file to other node
                Socket nodeSocket = UnicastUtils.connectSocket(ip, port);
                if(nodeSocket == null) continue;
                UnicastUtils.sendMessage(nodeSocket, ReplicationMessage.build(file.getName()));
                response = UnicastUtils.receiveMessage(nodeSocket).strip();
                if (response.contains("OK;")) {
                    UnicastUtils.sendMessage(
                            nodeSocket,
                            String.join("\n", FileUtils.readFromFile(file))
                    );
                }
                UnicastUtils.closeConnectionSocket(nodeSocket);
            }

            //delete file from curr node
            FileUtils.deleteFile(file);
        }
    }

}

