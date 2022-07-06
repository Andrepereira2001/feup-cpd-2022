package org.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class UnicastUtils {
    public static Socket connectSocket(InetAddress nodeIp, int nodePort){
        Socket s = null;
        try {
            s = new Socket(nodeIp, nodePort);
        } catch (Exception e){
            System.err.println("Failure to connect to socket " + nodeIp + ":" + nodePort + " " + e);
        }
        return s;
    }

    public static ServerSocket createSocket(int nodePort){
        ServerSocket s = null;
        try {
            s = new ServerSocket(nodePort);
        }
        catch (Exception e){
            System.err.println("Failure to create socket " + nodePort + " " + e);
        }
        return s;
    }

    public static void closeServerSocket(ServerSocket socket){
        try {
            socket.close();
        }
        catch (Exception e) {
            System.err.println("Error while closing server socket " + e);
        }
    }

    public static void closeConnectionSocket(Socket socket){
        try {
            socket.close();
        }
        catch (Exception e) {
            System.err.println("Error while closing socket " + e);
        }
    }

    public static boolean sendMessage(Socket socket, String message){
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        }
        catch (Exception e){
            System.err.println("Error while sending message " + message);
            System.err.println(e);
            return false;
        }
        return true;
    }

    public static String receiveMessage(Socket socket){
        //StringBuilder message = new StringBuilder();
        String message = "";
        int totalChars;
        char[] buffer = new char[32368];

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            totalChars = in.read(buffer,0,buffer.length);
            message = new String(buffer,0,totalChars);

        }catch (Exception e){
            System.err.println("Error while receiving message " + e);
            e.printStackTrace();
        }

        return message;
    }

}
