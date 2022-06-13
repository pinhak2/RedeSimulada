import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.CRC32;

public class RouterManager {

    private Router router;
    private final Scanner scanner;
    private String mark = "maquinanaoexiste";
    private Properties props;

    public RouterManager(Properties props) throws UnknownHostException {
        // declara socket cliente e obtem endereço IP do servidor com o DNS
        this.router = new Router(InetAddress.getByName("localhost"));
        // cria o stream do teclado
        this.scanner = new Scanner(System.in);
        this.props = props;
    }

    public void run() throws IOException {
        String ip_destino_do_token, apelido_da_maquina_atual, tempo_token;

        // Divisoria para ficar mais facil a visualização
        System.out.println("==================================================================");

        // Coleta de dados
        System.out.print("Informe o endereço IP da máquina que está a sua direita: ");
        ip_destino_do_token = this.scanner.nextLine();

        System.out.print("Informe um apelido: ");
        apelido_da_maquina_atual = this.scanner.nextLine();

        System.out.print("Informe o tempo do token e dos dados: ");
        tempo_token = this.scanner.nextLine();

        // Chamar metodo de salvar os dados
        updateConfigFile(ip_destino_do_token, apelido_da_maquina_atual, tempo_token);

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
                    if (routerNotConfigured()) {
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
                    String data = "2222;" + mark + ":" + port + ":" + destinationPort + ":" + crc32.getValue() + ":"
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
