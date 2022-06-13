import java.io.FileInputStream;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        try {
            Properties prop = new Properties();
            FileInputStream fileInputStream = new FileInputStream("config.properties");
            prop.load(fileInputStream);
            fileInputStream.close();
            new RouterManager(prop).run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
