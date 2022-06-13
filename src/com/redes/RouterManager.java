package com.redes;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class RouterManager {

    private Router router;
    private final Scanner scanner;

    public RouterManager() throws IOException {
        // declara socket cliente e obtem endereço IP do servidor com o DNS
        this.router = new Router(InetAddress.getByName("localhost"));
        // cria o stream do teclado
        this.scanner = new Scanner(System.in);
    }

    public void run() throws IOException {
        while (true) {
            System.out.println("Digite 1 para configurar uma porta local do roteador.");
            System.out.println("Digite 2 para configurar uma porta vizinha do roteador.");
            System.out.println("Digite 3 para enviar uma mensagem para um roteador.");
            System.out.println("Digite 4 para enviar um arquivo para um roteador.");
            System.out.println("Digite 5 para visualizar a tabela de roteamento do roteador.");
            System.out.print("Comando: ");
            String sentence = this.scanner.nextLine();

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
                    // Inicia thread para recener mensagens
                    new UnicastReceiver(this.router, this.router.getSockets().get(Integer.parseInt(destinationPort))).start();
                    // Inicia thread para enviar a tabela de roteamento para os roteadores vizinhos
                    new Rip(this.router).start();
                    // Inicia a thread para printar no console a tabela de roteamento a cada 10 segundos
//                    new PrintRoutingTable(this.router).start();
                    break;
                case "2":
                    System.out.print("Informe a porta de destino: ");
                    destinationPort = this.scanner.nextLine();
                    System.out.print("Informe a porta de saída: ");
                    String exitPort = this.scanner.nextLine();
                    System.out.print("Informe a porta local: ");
                    String localPort = this.scanner.nextLine();
                    // Cria um novo elemento da tabela de roteamento que possui a porta de destino, metrica, porta
                    // de saida e a porta local do roteador que possui a comunicação com essa porta de saída
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
                    String data = "::msg " + destinationPort + " " + message;
                    sendData = data.getBytes();
                    port = this.router.getExitPort(destinationPort);
                    if (port != null) {
                        // cria pacote com o dado, o endereço do server e porta do servidor
                        sendPacket = new DatagramPacket(sendData, sendData.length, this.router.getIPAddress(), port);
                        System.out.println(String.format(" Enviando mensagem para o destino %s pela porta %s", destinationPort, port));
                        //envia o pacote
                        socket = this.router.getSocketByPort(port);
                        if (socket != null) {
                            socket.send(sendPacket);
                        }
                    }
                    break;
                case "4":
                    if (this.routerNotConfigured()) {
                        System.out.println("Roteador não configurado!");
                        continue;
                    }
                    System.out.print("Informe a porta do roteador de destino: ");
                    destinationPort = this.scanner.nextLine();
                    System.out.print("Informe o caminho para o arquivo: ");
                    String path = this.scanner.nextLine();
                    Path file = Paths.get(path);
                    String fileName = Utils.formatFileName(file.getFileName().toString()) + " ";
                    byte[] fileBytes = Files.readAllBytes(file);
                    byte[] commandBytes = "::file ".getBytes();
                    byte[] destinationPortBytes = (destinationPort + " ").getBytes();
                    byte[] fileNameBytes = fileName.getBytes();
                    // Monta os dados a serem enviados
                    sendData = new byte[commandBytes.length + destinationPortBytes.length + fileNameBytes.length + fileBytes.length];
                    System.arraycopy(commandBytes, 0, sendData, 0, commandBytes.length);
                    System.arraycopy(destinationPortBytes, 0, sendData, commandBytes.length, destinationPortBytes.length);
                    System.arraycopy(fileNameBytes, 0, sendData, commandBytes.length + destinationPortBytes.length, fileNameBytes.length);
                    System.arraycopy(fileBytes, 0, sendData, commandBytes.length + destinationPortBytes.length + fileNameBytes.length, fileBytes.length);
                    port = this.router.getExitPort(destinationPort);

                    // cria pacote com o dado, o endereço do roteador e a porta de destino
                    sendPacket = new DatagramPacket(sendData, sendData.length, this.router.getIPAddress(), port);
                    System.out.println(String.format(" Enviando imagem para o destino %s pela porta %s", destinationPort, port));
                    //envia o pacote
                    socket = this.router.getSocketByPort(port);
                    if (socket != null) {
                        socket.send(sendPacket);
                    }
                    break;
                case "5":
                    System.out.println("\n\n##################################");
                    for (RoutingTable routingTable : this.router.getRoutingTable()) {
                        System.out.println(routingTable.getDestinationPort() + " " + routingTable.getMetric() + " " + routingTable.getExitPort());
                    }
                    System.out.println("##################################");
            }
        }
    }

    private boolean routerNotConfigured() {
        return this.router.getSockets().isEmpty();
    }
}
