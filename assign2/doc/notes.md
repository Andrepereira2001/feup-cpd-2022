- join e leave messages enviadas por multicast para todos os nodes do grupo
- multicast por udp (pode haver perda de mensagens, ordem trocada, etc)
- DatagramSocket - estabelece a comunicação cliente-servidor
- DatagramPacket - mensagem a transmitir através do canal
- código cliente e servidor na documentação (v17^)
- definir cliente na gama 224.0.0.0 - 239.255.255.255
- do lado de quem recebe (servidor) temos que fazer join explícito ao grupo a indicar que queremos receber mensagens do mesmo
- try catch para todas as operaçõs relacionadas com sockets!!
- 

## Service:

### Interfaces
- Tem de saber ler dos ficheiros / guardar / apagar (key-value store)
- Adicionar nós ao cluster / remover nós (cluster membership)

### Params
- IP-multicast
- Port of the IP-multicast
- Node id (IP da máquina)
- Port number (Port do server)

## Membership 

- Cada nó conhece os outros e tenta manter a sua informação atualizada.
- Sempre que um nó entra/saí no cluster envia um "Olá" (Join) ou um "Xau" (Leave) via multicast
- Mensagens incluem a membership counter - número indicador de presença no cluster, par presente, impar não presente. 
- Sempre que se recebe mensagem, atualizar um membership log, que está guardado em memória persistente

### Membership join

- Entrada num cluster, será necessário o envio dos atuais membros do cluster assim como uma lista (max 32) dos logs mais recentes por parte de um nó do cluster (via TCP), nó este que possui uma grande probabilidade de ter informação up-to-date (ESCOLHA DE CADA GRUPO) .
- Antes de enviar a mensagem de join o server aceita conecções numa porta (enviada na mensagem de join).
- Assim que uma mensagem de join é recebida um server espera um tempo random e envia a resposta para o nó, nessa mesma porta.
- O nó de entrada só aceita até três conecções.

- Em caso de falha (não receber as 3 mensagens), é necessário retransmitir a mensagem de join até uma maximo de três retransmissões, em que nós que já enviaram informação não reenviam a não ser que haja alguma mudança na informação a enviar.

### Membership update

- Devido há existencia de possiveis falhas, de segundo em segundo (como quem diz), será enviado um membership multicast por um dos nós dos bons.
- Recebida essa mensagem, cada nó dá update do seu membership log e o cluster membership.
- Membership log - mantém uma linha por nó com o maior número do counter, se o counter recebido for menor é rejeitado.

## Messages

### Format

- Mensagens char-based, exemplo no appendix.

## Key-value store

### Geração de keys e partitioning

- Keys são geradas com SHA-256 (assumir que não existem colisões).
- De forma a fazer a divisão dos key-value pairs a store usa **consistent hashing** (forma de alterar o nº de buckets).
- Hash function é também usada para calcular ids para cada nó de forma a gerar o assignment dos key-value-pairs (ler algoritmo de distâncias e tal).
- A hash recebe o id do nó que é passado por parametro da command line.

### Find the node responsible for the key-value

- Usar binary-search para pesquisar.
- Fazer pedidos (put e get) usando TCP.

- Quando a menbership é alterada, nós têm de transferir keys para outros nós.
- Num evento de join, o nó mais proximo do nó que se juntou deve transferir as keys que sao mais pequenas ou iguais ao id do nó que se juntou.
- Num evento de leave, o nó de saída transfere os pares para o nó mais proximo. 
- USAR TCP.

## Replication

### Prevent key-value pairs unavailability

Um par key-value deve ser guardado em 3 cluster nodes (replication factor = 3), pode ocorrer de um nó falhar, devendo tentar manter este número sempre igual.

Em vez de remover um par, deverá ser colocado um marker "tombstone" que indica que um par foi apagado.

## Fault-tolerance

10% adicionais se tratar estes cenários:
- Um nó falhar por mt tempo e falhar muitos eventos de membership, a membership periodica pode nao ser suficiente para que o nó aprenda o estado do cluster.
- Possibilidade de haver pedidos a nós não updated, que por sua vez, pedem a nós keys que já lá não se encontram.

## Test client 

Desenvolver client que procede a qualquer opereção.

2 interfaces - membership e pedidos a key-value

Pedidos put recebem um file path, o client computa a key com o value dentro do ficheiro e será printed para o output.
Get e delete recebem a key returnada pelo put.

Key value - TCP protocol
Membership - Devem ser enviadas para o nó especifico e podem usar qq transport protocol, mas queremos o RMI.

### Invocação

````shell
$ java TestClient node_ap operation [opnd]
````

Node access point, TCP ou UDP ip:port, RMI ip e o nome do remote object providing the service.

Operation, string com operação (put, delete, get, join, leave)

Operation arguments, apenas usado para key-value pair operations ou filepath ou hash.






