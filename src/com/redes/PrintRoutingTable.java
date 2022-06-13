package com.redes;

public class PrintRoutingTable extends Thread {

    private Router router;

    public PrintRoutingTable(Router router) {
        this.router = router;
    }

    public void run() {

        while (true) {
            // Se existir alguém na tabela de roteamento
            if (!this.router.getRoutingTable().isEmpty()) {
                // Exibe no console cada um dos elementos da tabela de roteamento
                System.out.println("\n\n##################################");
                for (RoutingTable rt : this.router.getRoutingTable()) {
                    System.out.println(rt.getDestinationPort() + " " + rt.getMetric() + " " + rt.getExitPort());
                }
                System.out.println("##################################");
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
