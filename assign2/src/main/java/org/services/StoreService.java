package org.services;

import org.node.messages.unicastMessages.DeleteMessage;
import org.node.messages.unicastMessages.GetMessage;
import org.node.messages.unicastMessages.PutMessage;
import org.utils.FileUtils;
import org.utils.UnicastUtils;
import org.utils.Utils;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private List<InetAddress> nodesIPs;
    private List<Integer> nodesPorts;

    public StoreService(String nodeID, int nodePort){
        this.nodesIPs = new ArrayList<>();
        this.nodesPorts = new ArrayList<>();
        try{
            this.nodesIPs.add(InetAddress.getByName(nodeID));
            this.nodesPorts.add(nodePort);
        }
        catch (Exception e){
            System.err.println("Error parsing node IP");
        }
    }

    public String put(String filePath){
        System.out.println("Put called in Store Service " + filePath);
        File file = FileUtils.getFileAbsolute(filePath);
        if(file.exists()){
            String data = String.join("\n",FileUtils.readFromFile(file));
            String hash = Utils.sha256(data + LocalDateTime.now());
            System.out.println("File key:");
            System.out.println(hash);
            putRequests(hash,data);
        }
        else {
            System.err.println("File could not be found.");
        }

        return "";
    }

    private void putRequests(String key, String data){
        String response;
        boolean sendingData = true;
        Socket nodeSocket;
        do {
            nodeSocket = this.connectNextSocket();
            if(nodeSocket == null) break;

            UnicastUtils.sendMessage(nodeSocket, PutMessage.build(key));
            response = UnicastUtils.receiveMessage(nodeSocket).strip();

            System.out.println("Response " + response);
            if (response.contains("OK;")) {
                UnicastUtils.sendMessage(nodeSocket, data);
                sendingData = false;
            } else if (verifyRedirect(response)) {
                UnicastUtils.closeConnectionSocket(nodeSocket);
            } else if (nodesIPs.isEmpty() && nodesPorts.isEmpty()){
                sendingData = false;
                System.err.println("Could not connect to any");
                UnicastUtils.closeConnectionSocket(nodeSocket);
            }
        }while (sendingData);
    }

    public String delete(String key){
        System.out.println("Delete called in Store Service");
        String response;
        boolean sendingData = true;
        Socket nodeSocket;

        do {
            nodeSocket = this.connectNextSocket();
            if(nodeSocket == null) break;
            UnicastUtils.sendMessage(nodeSocket, DeleteMessage.build(key));
            response = UnicastUtils.receiveMessage(nodeSocket).strip();
            UnicastUtils.closeConnectionSocket(nodeSocket);
            if(response.contains("OK;")){
                sendingData = false;
                System.out.println("File " + key + " deleted");
            }
            else if(verifyRedirect(response)){
                sendingData = true;
            }
            else if(response.contains("INVALID;")){
                System.out.println("File " + key + " do not exists");
                sendingData = false;
            }
            else if (nodesIPs.isEmpty() && nodesPorts.isEmpty()){
                sendingData = false;
                System.err.println("Could not connect to any node");
            }
        }while (sendingData);
        return "";
    }

    public String get(String key){
        System.out.println("Get called in Store Service");
        String response;
        boolean sendingData = true;
        Socket nodeSocket;

        do {
            nodeSocket = this.connectNextSocket();
            if(nodeSocket == null) break;
            UnicastUtils.sendMessage(nodeSocket, GetMessage.build(key));
            response = UnicastUtils.receiveMessage(nodeSocket).strip();
            if(response.contains("OK;")){
                response = UnicastUtils.receiveMessage(nodeSocket);
                File file = FileUtils.createFile("download");
                FileUtils.writeToFile(file,response,false);
                sendingData = false;
            }
            else if(verifyRedirect(response)){
                UnicastUtils.closeConnectionSocket(nodeSocket);
            }
            else if(response.contains("EXIST;")){
                sendingData = false;
                System.out.println("File " + key + " already exists.");
            }
            else if (nodesIPs.isEmpty() && nodesPorts.isEmpty()){
                sendingData = false;
                System.err.println("Could not connect to any node");
                UnicastUtils.closeConnectionSocket(nodeSocket);
            }
        }while (sendingData);


        return "";
    }

    private boolean verifyRedirect(String response){
        List<String> data = List.of(response.split(";"));
        if(data.get(0).equals("REDIRECT")){
            try {
                List<String> receivedAddresses = List.of(data.get(1).split(","));
                for(String elem : receivedAddresses){
                    List<String> address = List.of(elem.split(":"));
                    this.nodesIPs.add(InetAddress.getByName(address.get(0)));
                    this.nodesPorts.add(Integer.parseInt(address.get(1)));
                }
                return true;
            }catch (Exception e){
                System.err.println("Error while parsing redirect " + e);
            }
        }
        return false;
    }

    private Socket connectNextSocket(){
        Socket ret = null;
        do {
            System.out.println("Trying to connect to " + nodesIPs.get(0) + ":" + nodesPorts.get(0));
            ret = UnicastUtils.connectSocket(nodesIPs.get(0), nodesPorts.get(0));
            nodesPorts.remove(0);
            nodesIPs.remove(0);
        }
        while (ret == null && !nodesPorts.isEmpty() && !nodesIPs.isEmpty());
        return ret;
    }
}
