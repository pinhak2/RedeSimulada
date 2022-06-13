package com.redes;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

public class Rip extends Thread {

    private Router router;

    public Rip(Router router) {
        this.router = router;
    }

    public void run() {
        while (true) {
            // Obtém a tabela de roteamento do roteador
            List<RoutingTable> routers = this.router.getRoutingTable();
            // Obtém somente os elementos da tabela de roteamento que possuem ligação direta com o roteador
            List<RoutingTable> directRouters = this.router.getDirectPorts();
            try {
                // Se possuir elementos com ligação direta
                if (!directRouters.isEmpty()) {
                    // Serializa a tabela de roteamento inteira
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ObjectOutputStream outputStream = new ObjectOutputStream(out);
                    outputStream.writeObject(routers);
                    outputStream.flush();

                    // Pega os bytes da tabela de roteamento já serializada
                    byte[] listData = out.toByteArray();

                    // Percorre todas as ligações diretas configuradas no roteador
                    directRouters.stream().forEach(router -> {
                        // Monta o pacote
                        DatagramPacket packet = new DatagramPacket(listData, listData.length, this.router.getIPAddress(), Integer.parseInt(router.getExitPort()));
                        // Obtém o socket correto com base na configuração
                        DatagramSocket socket = this.router.getSockets().get(Integer.parseInt(router.getLocalPort()));
                        try {
                            // Envia o pacote
                            socket.send(packet);
                        } catch (IOException e) {
                            System.out.println("Error on sending message to port " + router.getLocalPort());
                        }
                    });
                }
                Thread.sleep(20000);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
