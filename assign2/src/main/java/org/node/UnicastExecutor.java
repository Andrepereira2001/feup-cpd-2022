package org.node;

import org.node.messages.unicastMessages.UnicastMessage;
import org.utils.UnicastUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class UnicastExecutor implements Runnable{
    private final Node node;
    private final ServerSocket unicastSocket;

    UnicastExecutor(Node node) {
        this.node = node;
        this.unicastSocket = node.getUnicastSocket();
    }

    @Override
    public void run() {

        while (!node.isShutdown()){
            try {
//                System.out.println("Waiting for new packet unicast2...");

                Socket socket = unicastSocket.accept();
//                System.out.println("Connection accepted2...");
                //s.setReuseAddress(true);

                String message  = UnicastUtils.receiveMessage(socket);
                if(!message.equals("")) {
                    node.execute(UnicastMessage.parseMessage(node,socket,message));
                }

//                System.out.println("Unicast2 message received  " + message);
            } catch (IOException e) {
                if(!node.isShutdown()) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

}
