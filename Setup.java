import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Setup {

    private final Scanner scanner;
    private static Properties props;
    private String token_props;

    public Setup(Properties props) {
        scanner = new Scanner(System.in);
        Setup.props = props;
    }

    public void run() {
        // Coleta de dados
        String ip_destino_do_token, apelido_da_maquina_atual, tempo_token;

        System.out.print("Informe o endereço IP da máquina que está a sua direita:");
        ip_destino_do_token = this.scanner.nextLine();

        System.out.print("Informe um apelido para estã maquina: ");
        apelido_da_maquina_atual = scanner.nextLine();

        System.out.print("Informe o tempo do token e dos dados: ");
        tempo_token = scanner.nextLine();

        // Chamar metodo de salvar os dados

        try {
            FileInputStream fileInputStream = new FileInputStream("config.properties");
            props.load(fileInputStream);
            fileInputStream.close();

            token_props = props.getProperty("token");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            updateConfigFile(ip_destino_do_token, apelido_da_maquina_atual, tempo_token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConfigFile(String ip_destino_do_token, String apelido_da_maquina_atual,
            String tempo_token) {
        try {

            // Abrir o arquivo de configuração para edição
            FileOutputStream out = new FileOutputStream("config.properties", true);

            // Alterar os valores correspondentes no arquivo
            props.setProperty("ip_destino_do_token", ip_destino_do_token);
            props.setProperty("apelido_da_maquina_atual", apelido_da_maquina_atual);
            props.setProperty("tempo_token", tempo_token);
            props.setProperty("token", token_props);

            // Salva as mudanças feitas sem comentarios adicionais
            props.store(out, null);

            // Fecha o editor
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
