package org.node.messages;

import org.node.Node;
import org.utils.FileUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.*;

public class MembershipMessage extends Message {
    List<String> members, log;

    public MembershipMessage(Node node, List<String> data) {
        super(node);
        this.members = List.of(data.get(2).split(","));
        this.log = List.of(data.get(3).split(","));
    }

    public static String build(InetAddress nodeIP, int nodePort, List<String> clusterMembers, List<String> membershipLogs) {
        return "MEMBERSHIP;" +
                nodeIP.getHostAddress() + ":" + nodePort + ";" +
                String.join(",", clusterMembers) + ";" +
                String.join(",", membershipLogs);
    }

    @Override
    public void run() {
        node.incrementMembershipMessages();
        updateMembership();
        updateMembers();
    }

    private void updateMembers() {
        File file = node.getNodeInfo().getClusterMembersFile();
        List<String> currentNodes = FileUtils.readFromFile(file);

        //union of all members
        Set<String> set = new HashSet<>();
        set.addAll(members);
        set.addAll(currentNodes);
        List<String> union = new ArrayList<String>(set);

        //intersection of both
        List<String> intersection = new ArrayList<>();
        for (String member: members) {
            if(currentNodes.contains(member)) {
                intersection.add(member);
            }
        }

        List<String> difference = new ArrayList<>(union);
        difference.removeAll(intersection);

        //only put the difference if the log counter indicates that is in
        File membershipFile = node.getNodeInfo().getMembershipFile();
        List<String> currentLogs = FileUtils.readFromFile(membershipFile);

        Map<String, Integer> fileMap = new HashMap<>();
        for(String fileLog: currentLogs){
            //log line from the MembershipMessage
            List<String> fileCounter = List.of(fileLog.split(" "));
            fileMap.put(fileCounter.get(0), Integer.parseInt(fileCounter.get(1)));
        }

        for(String currMember: difference){
            if(fileMap.get(currMember) % 2 == 0){
                intersection.add(currMember);
            }
        }

        FileUtils.writeToFile(file, String.join("\n", intersection) + "\n", false);
    }

    private void updateMembership() {
        List<String> currentLogs = node.getNodeInfo().getMembershipLogs();
        List<String> newLogs = new ArrayList<>();

        Map<String, Integer> fileMap = new HashMap<>();
        for(String fileLog: currentLogs){
            //log line from the MembershipMessage
            List<String> fileCounter = List.of(fileLog.split(" "));
            if(fileCounter.size() == 2) {
                fileMap.put(fileCounter.get(0), Integer.parseInt(fileCounter.get(1)));
            }

        }

        Map<String, Integer> messageMap = new HashMap<>();
        for(String currLog: log){
            //log line from the MembershipMessage
            List<String> currCounter = List.of(currLog.split(" "));
            if(currCounter.size() == 2) {
                messageMap.put(currCounter.get(0), Integer.parseInt(currCounter.get(1)));
            }
        }

        //update counter in those that are both in message and file
        for(String address: messageMap.keySet()){
            if(fileMap.containsKey(address)) {
                if (messageMap.get(address) > fileMap.get(address)) {
                    fileMap.put(address, messageMap.get(address));
                }
            } else {
                fileMap.put(address, messageMap.get(address));
            }
        }

        for(String address: fileMap.keySet()){
            newLogs.add(address + " " + fileMap.get(address));
        }

        node.getNodeInfo().setMembershipLog(String.join("\n", newLogs));

    }
}
