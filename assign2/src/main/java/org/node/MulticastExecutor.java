package org.node;

import org.node.messages.multicastMessages.MulticastMessage;
import org.utils.MulticastUtils;

import java.net.DatagramSocket;

public class MulticastExecutor implements Runnable{
    private final Node node;
    private final DatagramSocket multicastSocket;

    MulticastExecutor(Node node){
        this.node = node;
        this.multicastSocket = node.getMulticastSocket();
    }

    @Override
    public void run() {
        //implement with threads worker
        while (!node.isShutdown()){
//            System.out.println("Waiting for new packet multicast...");
            String message  = MulticastUtils.receiveMessage(multicastSocket);
//            System.out.println("Multicast message received  " + message);
            node.execute(MulticastMessage.parseMessage(node,message));
        }
    }

}
