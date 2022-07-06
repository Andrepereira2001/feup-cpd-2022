package org.node.messages.multicastMessages;

import org.node.Node;
import org.utils.Utils;

import java.net.InetAddress;
import java.util.List;

public class JoinMessage extends MulticastMessage {
    private final int counter;
    private final int joinsSent;

    public JoinMessage(Node node, InetAddress senderIP, int senderPort, List<String> data) {
        super(node, senderIP, senderPort);
        this.counter = Integer.parseInt(data.get(2));
        this.joinsSent = Integer.parseInt(data.get(3));
    }

    public static String build(InetAddress nodeIP, int nodePort, int counter, int joinsSent) {
        return "JOIN;" + nodeIP.getHostAddress() + ":" + nodePort + ";" + counter + ";" + joinsSent;
    }

    @Override
    public void run() {
        String senderAddress = this.getSenderIP().getHostAddress() + ":" + this.getSenderPort();
        this.node.getNodeInfo().addMembershipLog(senderAddress, this.counter);

        Utils.waitRandom(200);

        //adicionar novo node à lista com os current nodes que estão no cluster se ele não existir na lista (mensagem já tiver sido recebida)

        //update current nodes in cluster
        if (!this.node.getNodeInfo().isClusterMember(senderAddress)) {
            this.node.addMember(senderAddress);

            if(node.getPriority(node.getNodeInfo().getHashValue()) < joinsSent * 3) {
                this.node.sendMembershipMessage(this.getSenderIP(), this.getSenderPort());
            }

            this.node.getNodeStore().sendFilesToNewNode(this.getSenderIP(), this.getSenderPort());
        }
    }
}
