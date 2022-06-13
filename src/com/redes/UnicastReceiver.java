package com.redes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

public class UnicastReceiver extends Thread {

    private Router router;
    private DatagramSocket datagramSocket;
    private final int MAX_BUF = 65000;

    public UnicastReceiver(Router router, DatagramSocket socket) {
        this.router = router;
        this.datagramSocket = socket;
    }

    public void run() {
        while(true) {
            try {
                byte[] buf = new byte[MAX_BUF];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                // Recebe o pacote
                this.datagramSocket.receive(receivedPacket);
                String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                // Verifica do que se trata o pacote
                if (message.startsWith("::msg")) {
                    System.out.println("Pacote recebido da porta " + receivedPacket.getPort());
                    String[] splitMessage = message.split(" ");
                    message = this.getMessage(splitMessage);
                    String destinationPort = splitMessage[1];

                    // Verifica se o pacote é para o roteador
                    if (this.router.getSockets().get(Integer.parseInt(destinationPort)) != null) {
                        System.out.println("O pacote era para este roteador");
                        System.out.println("Mensagem recebida: " + message);
                    } else {
                        System.out.println("O pacote não era para este roteador");
                        // Monta novamente o pacote para ser enviado para o próximo roteador
                        String send = "::msg " + destinationPort + " " + message;
                        byte[] sendData = send.getBytes();
                        Integer port = this.router.getExitPort(destinationPort);
                        // cria pacote com o dado, o endereço do roteador e a porta de destino
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.router.getIPAddress(), port);
                        //envia o pacote
                        DatagramSocket socket = this.router.getSocketByPort(port);
                        System.out.println("Enviando para o destino pela porta " + socket.getLocalPort());
                        if (socket != null) {
                            socket.send(sendPacket);
                        }
                    }
                } else if (message.startsWith("::file")) {
                    System.out.println("Pacote recebido da porta " + receivedPacket.getPort());
                    String[] splitMessage = message.split(" ");
                    String fileName = splitMessage[2];
                    String destinationPort = splitMessage[1];

                    // Verifica se o pacote é para o roteador
                    if (this.router.getSockets().get(Integer.parseInt(destinationPort)) != null) {
                        System.out.println("O pacote era para este roteador");
                        byte[] data = receivedPacket.getData();
                        byte[] fileBytes = new byte[MAX_BUF];
                        int positionA = 0;
                        int positionB = 0;
                        // pega os bytes da imagem que estao logo apos dos bytes de "::file " e do nome do arquivo
                        for (byte b : data) {
                            if (positionB > 8 + destinationPort.length() + fileName.length()) {
                                fileBytes[positionA++] = b;
                            }
                            positionB++;
                        }
                        // cria diretorio para a porta do roteador caso nao exista e grava o arquivo na pasta
                        String dir = "src/com/redes/files/" + destinationPort;
                        Utils.createFile(fileBytes, dir, fileName);
                        System.out.println("Arquivo criado no diretório " + dir);
                    } else {
                        System.out.println("O pacote não era para este roteador");
                        byte[] sendData = receivedPacket.getData();
                        // Busca para qual porta deve ser enviado o pacote
                        Integer port = this.router.getExitPort(destinationPort);
                        // cria pacote com o dado, o endereço do roteador e a porta de destino
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.router.getIPAddress(), port);
                        System.out.println("Enviando para o destino pela porta " + this.datagramSocket.getLocalPort());
                        //envia o pacote
                        DatagramSocket socket = this.router.getSocketByPort(port);
                        if (socket != null) {
                            socket.send(sendPacket);
                        }
                    }
                } else {
                    // Deserializa tabela de roteamento
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(receivedPacket.getData()));
                    List<RoutingTable> list = (List<RoutingTable>) ois.readObject();
                    // Atualiza tabela de roteamento
                    this.router.updateRoutingTable(receivedPacket.getPort(), datagramSocket.getLocalPort(), list);
                    // Atualiza keep alive
                    this.router.alive(receivedPacket.getPort());
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMessage(String[] splitMessage) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < splitMessage.length; i++) {
            sb.append(splitMessage[i]);
            if (i + 1 != splitMessage.length) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
