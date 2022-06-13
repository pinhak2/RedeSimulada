import java.net.*;
import java.util.Properties;
import java.util.zip.CRC32;

public class RouterManager implements Runnable {

    private String holder = "maquinanaoexiste";
    Properties props;
    private int port = 6000;
    DatagramSocket socket;

    public RouterManager(Properties props) {
        // declara socket cliente e obtem endereço IP do servidor com o DNS
        // cria o stream do teclado
        this.props = props;
    }

    @Override
    public void run() {
        // Divisoria para ficar mais facil a visualização
        System.out.println("==================================================================");

        try {

            // Manter uma socket aberta para ouvir todas as requisições UDP
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

        } catch (Exception e) {
            e.printStackTrace();
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

                // Fazer o timeout baseado no config
                int timeout_time = Integer.parseInt(props.getProperty("tempo_token")) * 1000;
                socket.setSoTimeout(timeout_time);

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
                // Verificar se a mensagem foi enviada por está maquina
                if (verifySender(message)) {
                    System.out.println("Mensagem retornou!");
                    // Verificar a mensagem para saber se continua transmitindo
                    boolean continueInQueue = verifyHeaderFromMessage(message);

                    if (continueInQueue) {
                        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length,
                                ip_destino_config, port);
                        socket.send(sendPacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyHeaderFromMessage(String message) {
        String[] result = message.split(":");
        String[] resultFromStart = result[0].split(";");

        // Verificar o HEADER
        if (resultFromStart[1].equals("NAK")) {
            System.out.println("Houve um erro no pacote! Enviando mensagem novamente");
            return true;
        }
        if (resultFromStart[1].equals(holder)) {
            System.out.println("Não foi possivel encontrar a maquina destino!");
            return false;
        }
        System.out.println("Mensagem foi recebida pela maquina destino!");
        return false;
    }

    private boolean verifySender(String message) {
        // Formato do result
        // result[0] = 2222;maquinanaoexiste
        // result[1] = Nome_Origem
        // result[2] = Nome_Destino
        // result[3] = Crc32
        // result[4] = Mensagem
        String[] result = message.split(":");

        if (result[1].equals(props.getProperty("apelido_da_maquina_atual")))
            return true;

        return false;
    }

    private byte[] changeHeaderFromMessage(String message) {
        String[] result = message.split(":");

        if (!result[2].equals("TODOS")) {
            String[] resultFromStart = result[0].split(";");
            // Verificar se mensagem não possui erros
            CRC32 crc = new CRC32();
            // Gerando valor Crc32
            crc.update(result[4].getBytes());
            // Comparando com valor passado pela mensagem
            if (String.valueOf(crc.getValue()).equals(result[3])) {
                System.out.println("Mensagem correta! Mudando HEADER para ACK!");
                resultFromStart[1] = "ACK";
            } else {
                System.out.println("Erro na mensagem! Mudando HEADER para NAK!");
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
        System.out.println("Mensagem BROADCAST recebida!");
        return message.getBytes();
    }

    private boolean verifyRecipient(String message) {
        // Formato do result
        // result[0] = 2222;maquinanaoexiste
        // result[1] = Nome_Origem
        // result[2] = Nome_Destino
        // result[3] = Crc32
        // result[4] = Mensagem
        String[] result = message.split(":");

        if ((result[2].equals(props.getProperty("apelido_da_maquina_atual")))
                || result[2].equals(props.getProperty("TODOS")))
            return true;

        return false;
    }
}
