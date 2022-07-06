# Proj. 2: Distributed and Partitioned Key-Value Store

## Group:
- André Pereira - up201905650
- Margarida Vieria - up201907907
- Matilde Oliveira - up201906954

## Compilation and execution with IntelIJ UI

In order to execute our project you can open it in the intelIJ and execute the already created run configurations.

We recommend this approach due to its simplicity.

## Compilation and execution with CMD

In order to compile and execute our project in the cmd you must be in the project folder __assign2__. 

### Compilation

To compile the code access to gradle is needed. It can either be compiled with the command ``gradle build`` or 
by using the build functionality of intelIJ (Crtl + f9). 
After the compilation a folder named __build__ must be created inside the project folder

### Execution

To easily execute the code we build 3 different scripts being ``.\node.bat`` ``.\client.bat`` ``.\delete.bat``

#### Node
In order to start a single node run the following command:

``.\node.bat <mcast_ip> <mcast_port> <node_ip> <node_port>``

Where:
- `<mcast_ip>` is the multicast ip of the cluster
- `<mcast_port>` is the multicast port of the cluster
- `<node_ip>` is the node ip
- `<node_port>` is the node port

Example: ``.\node.bat 227.10.10.10 8080 127.10.10.1 5000``

#### TestClient

In order to start a client and send a request to a node run the following command:

``.\client.bat <node_ip:node_port> <operation> [<opnd>]``

Where:
- `<node_ip:node_port>` is the node address, so node ip followed by the node port
- `<operation>` is the operation to be performed put, get, delete, join, leave
- `[<opnd>]` is the argument of the operation can be a file path in a put or a hash in get e delete

Examples:
```
.\client.bat 127.10.10.1:5000 put C:\Users\adbp\Documents\FEUP\3ano\2semestre\CPD\g04\assign2\src\data\file.txt

.\client.bat 127.10.10.1:5000 get 04dc59fad483eeccd3dda33543b91fae9ca276e05ce6b1116c7daae856735a16

.\client.bat 127.10.10.1:5000 join    

.\client.bat 127.10.10.1:5000 leave
```

#### Store
In order to delete all files and info in the storage node run the following command:
`` .\delete.bat ``