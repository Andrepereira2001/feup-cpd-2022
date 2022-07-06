package org.node;

import org.utils.FileUtils;
import org.utils.Utils;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeInfo {
    private final InetAddress mcastAddr;
    private final int mcastPort;
    private final InetAddress nodeIP;
    private final int nodePort;

    public NodeInfo(InetAddress mcastAddr, int mcastPort, InetAddress nodeIP, int nodePort) {
        this.mcastAddr = mcastAddr;
        this.mcastPort = mcastPort;
        this.nodeIP = nodeIP;
        this.nodePort = nodePort;
    }

    public InetAddress getMcastAddr() {
        return mcastAddr;
    }

    public int getMcastPort() {
        return mcastPort;
    }

    public InetAddress getNodeIP() {
        return nodeIP;
    }

    public int getNodePort() {
        return nodePort;
    }

    public File getMembershipFile() {
        return FileUtils.createFile("store/" + this.toString() + "/membership.txt");
    }

    public File getClusterMembersFile() {
        return FileUtils.createFile("store/" + this.toString() + "/members.txt");
    }

    public File getCounterFile() {
        return FileUtils.createFile("store/" + this + "/counter.txt");
    }

    public List<String> getClusterMembers() {
        File membersFile = getClusterMembersFile();
        return FileUtils.readFromFile(membersFile);
    }

    public String getHashValue() {
        return Utils.sha256(getAddress());
    }

    public String getAddress() {
        return nodeIP.getHostAddress() + ":" + nodePort;
    }

    public int getNodeCounter() {
        List<String> allData = FileUtils.readFromFile(this.getCounterFile());
        String data = "";

        if (!allData.isEmpty()) {
            data = allData.get(0);
        }

        int membershipCounter = 0;
        if (!data.equals("")) {
            membershipCounter = Integer.parseInt(data);
        }

        return membershipCounter;
    }

    public void setNodeCounter(int counter) {
        FileUtils.writeToFile(this.getCounterFile(), String.valueOf(counter), false);
    }

    public void addClusterMember(String address) {
        File membersFile = getClusterMembersFile();
        FileUtils.writeToFile(membersFile, address + "\n", true);
    }

    public boolean isClusterMember(String address) {
        File membersFile = getClusterMembersFile();
        List<String> currNodes = FileUtils.readFromFile(membersFile);
        return currNodes.contains(address);
    }

    public void removeClusterMember(String address) {
        File file = this.getClusterMembersFile();

        List<String> logs = FileUtils.readFromFile(file);

        logs.remove(address);

        String joinedString = String.join("\n", logs);
        FileUtils.writeToFile(file, joinedString + "\n", false);
    }

    public List<String> getMembershipLogs() {
        File logFile = getMembershipFile();
        List<String> lines = FileUtils.readFromFile(logFile);
        if (lines.size() > 32) {
            lines = lines.subList(lines.size() - 32, lines.size());
        }
        return lines;
    }

    public void setMembershipLog(String message) {
        File logFile = getMembershipFile();
        FileUtils.writeToFile(logFile, message + "\n", false);
    }

    public int getNodeCountersSum(){
        List<String> logs = this.getMembershipLogs();
        int counter=0;
        for(String log: logs){
            List<String> val = List.of(log.split(" "));
            if(val.size() == 2) {
                counter += Integer.parseInt(val.get(1));
            }
        }
        return counter;
    }


    public void addMembershipLog(String address, int counter) {
        List<String> logs = getMembershipLogs();
        String newLog = address + " " + counter;
        List<String> newLogs = new ArrayList<>();

        boolean addNewLog = true;
        for (String log : logs) {
            if (!log.contains(address)) {
                newLogs.add(log);
            } else {
                List<String> counterInfo = List.of(log.split(" "));
                if (Integer.parseInt(counterInfo.get(1)) >= counter) {
                    newLogs.add(log);
                    addNewLog = false;
                }
            }
        }

        if (addNewLog) {
            newLogs.add(newLog);
        }

        String joinedString = String.join("\n", newLogs);
        this.setMembershipLog(joinedString);
    }

    @Override
    public String toString() {
        return "org/node" + nodeIP.getHostAddress() + "_" + nodePort +
                "mcast" + mcastAddr.getHostAddress() + "_" + mcastPort;
    }

    public boolean isEqual(InetAddress ip, int port) {
        return Objects.equals(ip, nodeIP) && port == nodePort;
    }
}
