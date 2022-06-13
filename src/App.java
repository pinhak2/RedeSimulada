import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;

public class App {
    private Router router;
    private final Scanner scanner;
    private String holder = "maquinanaoexiste";

    public App() throws IOException {
        this.router = new Router(InetAddress.getByName("localhost"));
        // cria o stream do teclado
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {

        Properties prop = new Properties();
        try {
            new RouterManager().run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
