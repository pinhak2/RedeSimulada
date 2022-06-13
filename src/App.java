import java.io.FileInputStream;
import java.util.Properties;

public class App {
    static Properties prop = new Properties();

    public static void main(String[] args) {
        try {
            FileInputStream fileInputStream = new FileInputStream("config.properties");
            prop.load(fileInputStream);
            fileInputStream.close();
            // new RouterManager(prop).run();
            final RouterManager INSTANCE = new RouterManager(prop);
            INSTANCE.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
