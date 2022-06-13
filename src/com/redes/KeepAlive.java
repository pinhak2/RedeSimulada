package com.redes;

import java.util.concurrent.ConcurrentHashMap;

public class KeepAlive extends Thread{

    ConcurrentHashMap<Integer, Integer> times;

    Integer period = 5;

    Router router;

    public KeepAlive(Router router) {
        this.times = new ConcurrentHashMap<>();
        this.router = router;
    }

    public void alive(int port) {
        times.put(port, 0);
    }

    public void run() {
        while (true) {
            try {
                // Atualiza o tempo de keep alive
                times.entrySet()
                        .forEach(e -> times.put(e.getKey(), e.getValue() + period));

                // Printa na tela os tempos atuais de keep alive das portas ativas no roteador
//                times.entrySet()
//                        .forEach(e -> System.out.println(e.getKey() + " | " + e.getValue()));

                // Se tiver alguma porta com tempo maior que 30 segundos, então remove ela do roteador
                times.entrySet()
                        .stream().filter(p -> p.getValue() >= 30)
                        .forEach(f -> router.disable(f.getKey()));

                Thread.sleep(period * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
