import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.CRC32;

public class RouterManager implements Runnable {

    private Router router;
    private final Scanner scanner;
    private String holder = "maquinanaoexiste";
    Properties props;
    private int port = 6000;
    DatagramSocket socket;

    public RouterManager(Properties props) throws UnknownHostException {
        // declara socket cliente e obtem endereço IP do servidor com o DNS
        this.router = new Router(InetAddress.getByName("localhost"));
        // cria o stream do teclado
        this.scanner = new Scanner(System.in);
        this.props = props;
    }

    @Override
    public void run() {
        String ip_destino_do_token, apelido_da_maquina_atual, tempo_token;

        // Divisoria para ficar mais facil a visualização
        System.out.println("==================================================================");

        // Coleta de dados
        System.out.print("Informe o endereço IP da máquina que está a sua direita:");
        ip_destino_do_token = this.scanner.nextLine();

        System.out.print("Informe um apelido para estã maquina: ");
        apelido_da_maquina_atual = this.scanner.nextLine();

        System.out.print("Informe o tempo do token e dos dados: ");
        tempo_token = this.scanner.nextLine();

        // Chamar metodo de salvar os dados
        try {
            updateConfigFile(ip_destino_do_token, apelido_da_maquina_atual, tempo_token);

            // Manter uma socket aberta para ouvir todas as requisições UDP
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Verifica se tem token ativo
        if (Boolean.valueOf(props.getProperty("token"))) {
            String tokenMessage = "1111";
            byte[] tokenByte;

            tokenByte = tokenMessage.getBytes();

            DatagramPacket packet;
            try {
                packet = new DatagramPacket(tokenByte, tokenByte.length,
                        InetAddress.getByName(props.getProperty("ip_destino_do_token")), port);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Token enviado para a rede!");

            try (FileOutputStream out = new FileOutputStream("config.properties")) {
                // Alterar valor do token para não enviar mais
                props.setProperty("token", "false");

                // Salva as mudanças feitas sem comentarios adicionais
                props.store(out, null);

                // Fecha o editor
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        while (true) {

            System.out.println(getClass().getName() + " >>>Pronto para receber pacotes!");
            try {
                InetAddress ip_destino_config;
                ip_destino_config = InetAddress.getByName(props.getProperty("ip_destino_do_token"));

                // Receber pacote
                byte[] recvBuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                // Pacote recebido
                System.out.println(getClass().getName() + " >>>Pacote descoberto! Veio de: "
                        + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + " >>>Pacote recebido; data: " + new String(packet.getData()));

                String message = new String(packet.getData()).trim();

                // Veja se a mensagem é para esta maquina
                if (verifyRecipient(message)) {
                    System.out.println("Está maquina é o computador destino!");

                    // AlteraCabeçalho da mensagem
                    byte[] sendData = changeHeaderFromMessage(message);

                    // Continua enviando na ordem pre-definida
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip_destino_config, port);
                    socket.send(sendPacket);

                    System.out.println(
                            getClass().getName() + " >>>Pacote enviado para: "
                                    + ip_destino_config.toString());
                }

                String nickname, messageToSend;

                System.out.println("Digite o apelido da maquina para que deseja enviar");
                nickname = this.scanner.nextLine();

                System.out.println("Digite a mensagem que deseja enviar");
                messageToSend = this.scanner.nextLine();

                byte[] messageByte = messageToSend.getBytes();
                // Faz a validação com o Crc32
                CRC32 crc32 = new CRC32();
                crc32.update(messageByte);
                // Prepara a mensagem a ser enviada
                String data = "2222;" + holder + ":" + props.getProperty("apelido_da_maquina_atual") + ":" + nickname
                        + ":"
                        +
                        crc32.getValue() + ":"
                        + ":" + messageToSend;
                byte[] sendDataMessage = data.getBytes();
                // cria pacote com o dado, o endereço do server e porta do servidor
                DatagramPacket sendPacket = new DatagramPacket(sendDataMessage, sendDataMessage.length,
                        ip_destino_config, port);
                socket.send(sendPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
            // while (true) {
            // System.out.println("==================================================================");
            // System.out.println("Digite 1 para configurar uma porta local do roteador.");
            // System.out.println("Digite 2 para configurar uma porta vizinha do
            // roteador.");
            // System.out.println("Digite 3 para enviar uma mensagem para um roteador.");
            // System.out.println("Digite 4 para visualizar a tabela de roteamento do
            // roteador.");
            // System.out.print("Comando: ");
            // String sentence = this.scanner.nextLine();
            // System.out.println("");
            // byte[] sendData = null;
            // Integer port = null;
            // RoutingTable rt = null;
            // DatagramSocket socket = null;
            // String destinationPort = null;
            // DatagramPacket sendPacket = null;
            // switch (sentence) {
            // case "1":
            // // Deixa serem configuradas somente duas portas
            // if (this.router.getLocalPorts().size() == 2) {
            // System.err.println("Todas as portas disponíveis já estão configuradas.");
            // continue;
            // }
            // System.out.print("Informe a porta de destino: ");
            // destinationPort = this.scanner.nextLine();
            // rt = new RoutingTable(destinationPort, 0, "Local");
            // // Cria o socket refernte a porta configurada
            // this.router.addSocket(Integer.parseInt(destinationPort));
            // // Adiciona a porta na tabela de roteamento
            // this.router.addPort(rt);
            // // Inicia thread para receber mensagens
            // new UnicastReceiver(this.router,
            // this.router.getSockets().get(Integer.parseInt(destinationPort)))
            // .start();
            // // Inicia thread para enviar a tabela de roteamento para os roteadores
            // vizinhos
            // new Rip(this.router).start();

            // break;
            // case "2":
            // System.out.print("Informe a porta de destino: ");
            // destinationPort = this.scanner.nextLine();
            // System.out.print("Informe a porta de saída: ");
            // String exitPort = this.scanner.nextLine();
            // System.out.print("Informe a porta local: ");
            // String localPort = this.scanner.nextLine();
            // // Cria um novo elemento da tabela de roteamento que possui a porta de
            // destino,
            // // metrica, porta
            // // de saida e a porta local do roteador que possui a comunicação com essa
            // porta
            // // de saída
            // rt = new RoutingTable(destinationPort, 1, exitPort, localPort);
            // // Adiciona a porta na tabela de roteamento
            // this.router.addPort(rt);
            // break;
            // case "3":
            // if (routerNotConfigured()) {
            // System.out.println("Roteador não configurado!");
            // continue;
            // }
            // System.out.print("Informe a porta do roteador de destino: ");
            // destinationPort = this.scanner.nextLine();
            // System.out.print("Informe a mensagem: ");
            // String menssagem = this.scanner.nextLine();
            // port = this.router.getExitPort(destinationPort);
            // byte[] messageByte = menssagem.getBytes();
            // CRC32 crc32 = new CRC32();
            // crc32.update(messageByte);
            // String data = "2222;" + holder + ":" + port + ":" + destinationPort + ":" +
            // crc32.getValue() + ":"
            // + ":" + menssagem;
            // sendData = data.getBytes();
            // if (port != null) {
            // // cria pacote com o dado, o endereço do server e porta do servidor
            // sendPacket = new DatagramPacket(sendData, sendData.length,
            // this.router.getIPAddress(), port);
            // System.out.println(this.router.getIPAddress());
            // System.out.println(String.format(" Enviando mensagem para o destino %s pela
            // porta %s",
            // destinationPort, port));
            // // envia o pacote
            // socket = this.router.getSocketByPort(port);
            // if (socket != null) {
            // socket.send(sendPacket);
            // }
            // }
            // break;
            // case "4":
            // System.out.println("\n\n##################################");
            // for (RoutingTable routingTable : this.router.getRoutingTable()) {
            // System.out.println(routingTable.getDestinationPort() + " " +
            // routingTable.getMetric() + " "
            // + routingTable.getExitPort());
            // }
            // System.out.println("##################################");
            // }
            // }
        }
    }

    private byte[] changeHeaderFromMessage(String message) {
        String[] result = message.split(":");
        String[] resultFromStart = result[0].split(";");

        // Verificar se mensagem não possui erros
        CRC32 crc = new CRC32();
        // Gerando valor Crc32
        crc.update(result[4].getBytes());
        // Comparando com valor passado pela mensagem
        if (String.valueOf(crc.getValue()).equals(result[3])) {
            resultFromStart[1] = "ACK";
        } else {
            resultFromStart[1] = "NAK";
        }
        // Reformando a mensagem
        String newMessage = "2222;" + resultFromStart[1] + ":" + result[1] + ":" + result[2]
                + ":"
                +
                result[3] + ":"
                + ":" + result[4];

        return newMessage.getBytes();
    }

    private boolean verifyRecipient(String message) {
        // Formato do result
        // result[0] = 2222;maquinanaoexiste
        // result[1] = Nome_Origem
        // result[2] = Nome_Destino
        // result[3] = Crc32
        // result[4] = Mensagem
        String[] result = message.split(":");

        if (result[2].equals(props.getProperty("apelido_da_maquina_atual")))
            return true;

        return false;
    }

    public boolean routerNotConfigured() {
        return this.router.getSockets().isEmpty();
    }

    private void updateConfigFile(String ip_destino_do_token, String apelido_da_maquina_atual, String tempo_token)
            throws IOException {
        // Abrir o arquivo de configuração para edição
        FileOutputStream out = new FileOutputStream("config.properties");

        // Alterar os valores correspondentes no arquivo
        props.setProperty("ip_destino_do_token", ip_destino_do_token);
        props.setProperty("apelido_da_maquina_atual", apelido_da_maquina_atual);
        props.setProperty("tempo_token", tempo_token);

        // Salva as mudanças feitas sem comentarios adicionais
        props.store(out, null);

        // Fecha o editor
        out.close();
    }
}
