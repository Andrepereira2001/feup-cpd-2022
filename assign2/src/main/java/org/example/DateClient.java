package org.example;

import java.io.IOException;
import java.net.*;

public class DateClient {

    // Used in leaveGroup()
    static NetworkInterface inIf = null;

    static DatagramSocket join(InetAddress group, int port, String netIfStr){
        DatagramSocket s = null;

        try {
            s = new DatagramSocket(null);
            s.setReuseAddress(true);
            s.bind(new InetSocketAddress(port));
            inIf = NetworkInterface.getByName(netIfStr);
            s.joinGroup(new InetSocketAddress(group,0),inIf);
        } catch (Exception e){
            System.err.println("Failure to join multicast group " + group + ":" + port);
            System.err.println(e);
            s = null;
        }

        return s;
    }


    public static void main(String[] args) throws IOException {

        DatagramSocket s = null;
        int port = 0;
        InetAddress group = null;
        String netIfStr = null;
        int no_packets = Integer.MAX_VALUE;


        System.out.println("len ------- " + args.length);
        System.out.println("1 ------- " + args[0]);
        System.out.println("2 ------- " + args[1]);
        System.out.println("3 ------- " + args[2]);

        if(args.length != 3 && args.length != 4){
            System.out.println("Usage: java DateClient <multicast_group> <multicas_port> <net_interf> [<no_packets>]");
            System.out.println("\t Use \"\" (empty string) for <net_interf> to not to configure the network interface");
            System.exit(1);
        }

        try {
            group = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        } catch (UnknownHostException e){
            System.err.println("Don't know about host: " + args[0]);
            System.exit(1);
        }
        catch (NumberFormatException e){
            System.err.println("Error: " + args[1] + " is not a properly formatted integer");
            System.exit(1);
        }

        if( port > 65535 || port < 0){
            System.out.println("Error: port number must be a non-negative integer smaller then 65536");
            System.exit(1);
        }

        netIfStr = args[2];

        if( args.length == 4){
            try {
                no_packets = Integer.parseInt(args[3]);
            } catch (NumberFormatException e){
                System.err.println("Error: " + args[3] + " is supposed to be a number of packets. Using " + no_packets);
            }
        }

        s = join(group, port, netIfStr);

        if(s == null){
            System.err.println("Error joining multicast address");
            System.exit(1);
        }

        for( int n = 0; n < 10; n++){
            DatagramPacket recvPacket = null;
            byte[] recvBuffer = new byte[8092];
            String date;

            recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
            System.out.println("");
            System.out.println("Waiting for new packet...");
            s.receive(recvPacket);
            date = new String(recvPacket.getData(), 0 , recvPacket.getLength());
            System.out.println("Right noew @ " + recvPacket.getAddress() + " it is " + date);
        }

        try{
            s.leaveGroup(new InetSocketAddress(group,0), inIf);
        } catch (Exception e){
            System.out.println("Failure to leave "+ group + " : " + e);
        }

    }
}
