package org.client;
import org.services.MembershipService;
import org.services.StoreService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TestClient {

    public static void main(String[] args) {

        //args tem que ter pelo menos 2 o node_ap e a operation e pode ter opnd
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java TestClient <node_ap> <operation> [<opnd>]");
            return;
        }

        MembershipService service;

        try {

            String nodeap = args[0];
            String nodeIP = nodeap.substring(0, nodeap.lastIndexOf(":"));
            int nodePort = Integer.parseInt(nodeap.substring(nodeap.lastIndexOf(":")+1));

            String operation = args[1];

            String operand = "";
            if(args.length == 3) {
                operand = args[2]; //pode ser a key ou o filename dependendo da operação
            }

            System.out.println("Calling node IP " + nodeIP + " in port " + nodePort + " operation " + operation + " " + operand );

            Registry registry = LocateRegistry.getRegistry(nodePort + 300);
            service = (MembershipService) registry.lookup(nodeIP + nodePort);

            if (operation.equalsIgnoreCase("JOIN"))
                service.join();
            else if (operation.equalsIgnoreCase("LEAVE"))
                service.leave();
            else if (operation.equalsIgnoreCase("PUT")){
                StoreService storeService = new StoreService(nodeIP,nodePort);
                storeService.put(operand);
            }
            else if (operation.equalsIgnoreCase("GET")){
                StoreService storeService = new StoreService(nodeIP,nodePort);
                storeService.get(operand);
            }
            else if (operation.equalsIgnoreCase("DELETE")){
                StoreService storeService = new StoreService(nodeIP,nodePort);
                storeService.delete(operand);
            }
            else
                System.err.println("Operation not valid");

        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }

}
