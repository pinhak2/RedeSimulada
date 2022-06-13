import java.util.Properties;

public class App {
    static Properties prop = new Properties();

    public static void main(String[] args) {
        try {
            // new RouterManager(prop).run();
            new Setup(prop).run();
            if (Boolean.valueOf(prop.getProperty("token")))
                new Sender(prop).run();
            final RouterManager INSTANCE = new RouterManager(prop);
            INSTANCE.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
