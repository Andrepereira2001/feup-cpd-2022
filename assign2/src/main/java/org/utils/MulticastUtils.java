package org.utils;

import java.net.*;

public class MulticastUtils {
    public static DatagramSocket createDatagram(InetAddress groupIp, int port, NetworkInterface networkInterface){
        DatagramSocket s = null;

        try {
            s = new DatagramSocket(null);
            s.setReuseAddress(true);
            s.bind(new InetSocketAddress(port));
            s.joinGroup(new InetSocketAddress(groupIp,0),networkInterface);
        } catch (Exception e){
            System.err.println("Failure to join multicast group " + groupIp + ":" + port + " " + e);
        }

        return s;
    }

    public static NetworkInterface createNetworkInterface(String netIfStr){
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName(netIfStr);
        }
        catch (Exception e){
            System.err.println("Error while creating interface " + e);
        }

        return networkInterface;
    }

    public static void closeDatagram(DatagramSocket socket, InetAddress groupIp, int port, NetworkInterface networkInterface){
        try {
            socket.leaveGroup(new InetSocketAddress(groupIp,port), networkInterface);
        }
        catch (Exception e) {
            System.err.println("Error while closing datagram socket " + e);
        }
    }

    public static boolean sendMessage(DatagramSocket socket, String message, InetAddress groupIp, int port){
        DatagramPacket packet = null;
        byte[] buffer = null;

        buffer = message.getBytes();
        packet = new DatagramPacket(buffer, buffer.length, groupIp, port);

        try {
            socket.send(packet);
        }catch (Exception e){
            System.err.println("Error while sending message " + message);
            System.err.println(e);
            return false;
        }
        return true;
    }

    public static String receiveMessage(DatagramSocket socket){
        String message = null;
        DatagramPacket packet = null;
        byte[] buffer = new byte[32368];

        try {
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            message = new String(packet.getData(),0,packet.getLength());
        }catch (Exception e){
            System.err.println("Error while receiving message " + e);
        }

        return message;
    }
}
