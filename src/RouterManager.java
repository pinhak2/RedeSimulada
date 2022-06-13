import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.zip.CRC32;

public class RouterManager {

    private Router router;
    private final Scanner scanner;
    private String holder = "maquinanaoexiste";

    public RouterManager() throws IOException {
        // declara socket cliente e obtem endereço IP do servidor com o DNS
        this.router = new Router(InetAddress.getByName("localhost"));
        // cria o stream do teclado
        this.scanner = new Scanner(System.in);
    }

    public void run() throws IOException {
        while (true) {
            System.out.println("==================================================================");
            System.out.println("Digite 1 para configurar uma porta local do roteador.");
            System.out.println("Digite 2 para configurar uma porta vizinha do roteador.");
            System.out.println("Digite 3 para enviar uma mensagem para um roteador.");
            System.out.println("Digite 4 para visualizar a tabela de roteamento do roteador.");
            System.out.print("Comando: ");
            String sentence = this.scanner.nextLine();
            System.out.println("");
            byte[] sendData = null;
            Integer port = null;
            RoutingTable rt = null;
            DatagramSocket socket = null;
            String destinationPort = null;
            DatagramPacket sendPacket = null;
            switch (sentence) {
                case "1":
                    // Deixa serem configuradas somente duas portas
                    if (this.router.getLocalPorts().size() == 2) {
                        System.err.println("Todas as portas disponíveis já estão configuradas.");
                        continue;
                    }
                    System.out.print("Informe a porta de destino: ");
                    destinationPort = this.scanner.nextLine();
                    rt = new RoutingTable(destinationPort, 0, "Local");
                    // Cria o socket refernte a porta configurada
                    this.router.addSocket(Integer.parseInt(destinationPort));
                    // Adiciona a porta na tabela de roteamento
                    this.router.addPort(rt);
                    // Inicia thread para receber mensagens
                    new UnicastReceiver(this.router, this.router.getSockets().get(Integer.parseInt(destinationPort)))
                            .start();
                    // Inicia thread para enviar a tabela de roteamento para os roteadores vizinhos
                    new Rip(this.router).start();

                    break;
                case "2":
                    System.out.print("Informe a porta de destino: ");
                    destinationPort = this.scanner.nextLine();
                    System.out.print("Informe a porta de saída: ");
                    String exitPort = this.scanner.nextLine();
                    System.out.print("Informe a porta local: ");
                    String localPort = this.scanner.nextLine();
                    // Cria um novo elemento da tabela de roteamento que possui a porta de destino,
                    // metrica, porta
                    // de saida e a porta local do roteador que possui a comunicação com essa porta
                    // de saída
                    rt = new RoutingTable(destinationPort, 1, exitPort, localPort);
                    // Adiciona a porta na tabela de roteamento
                    this.router.addPort(rt);
                    break;
                case "3":
                    if (this.routerNotConfigured()) {
                        System.out.println("Roteador não configurado!");
                        continue;
                    }
                    System.out.print("Informe a porta do roteador de destino: ");
                    destinationPort = this.scanner.nextLine();
                    System.out.print("Informe a mensagem: ");
                    String message = this.scanner.nextLine();
                    port = this.router.getExitPort(destinationPort);
                    byte[] messageByte = message.getBytes();
                    CRC32 crc32 = new CRC32();
                    crc32.update(messageByte);
                    String data = "2222;" + holder + ":" + port + ":" + destinationPort + ":" + crc32.getValue() + ":"
                            + ":" + message;
                    sendData = data.getBytes();
                    if (port != null) {
                        // cria pacote com o dado, o endereço do server e porta do servidor
                        sendPacket = new DatagramPacket(sendData, sendData.length, this.router.getIPAddress(), port);
                        System.out.println(this.router.getIPAddress());
                        System.out.println(String.format(" Enviando mensagem para o destino %s pela porta %s",
                                destinationPort, port));
                        // envia o pacote
                        socket = this.router.getSocketByPort(port);
                        if (socket != null) {
                            socket.send(sendPacket);
                        }
                    }
                    break;
                case "4":
                    System.out.println("\n\n##################################");
                    for (RoutingTable routingTable : this.router.getRoutingTable()) {
                        System.out.println(routingTable.getDestinationPort() + " " + routingTable.getMetric() + " "
                                + routingTable.getExitPort());
                    }
                    System.out.println("##################################");
            }
        }
    }

    private boolean routerNotConfigured() {
        return this.router.getSockets().isEmpty();
    }
}
