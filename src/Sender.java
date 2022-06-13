import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.CRC32;

public class Sender extends Thread {
        private final Scanner scanner;
        private String holder = "maquinanaoexiste";
        Properties props;
        private int port = 6000;
        DatagramSocket socket;

        public Sender(Properties props) {
                // declara socket cliente e obtem endereço IP do servidor com o DNS
                // cria o stream do teclado
                this.scanner = new Scanner(System.in);
                this.props = props;
        }

        public void run() {

                try {

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

                        // Abrir uma porta randomica para enviar os pacotes
                        DatagramSocket c = new DatagramSocket();
                        c.setBroadcast(true);

                        InetAddress ip_destino_config;
                        ip_destino_config = InetAddress.getByName(props.getProperty("ip_destino_do_token"));

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
                        String data = "2222;" + holder + ":" + props.getProperty("apelido_da_maquina_atual") + ":"
                                        + nickname
                                        + ":"
                                        +
                                        crc32.getValue() + ":"
                                        + ":" + messageToSend;
                        byte[] sendDataMessage = data.getBytes();
                        // Cria pacote com o dado, o endereço do server e porta do servidor
                        DatagramPacket sendPacket = new DatagramPacket(sendDataMessage, sendDataMessage.length,
                                        ip_destino_config, port);
                        c.send(sendPacket);
                        c.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
