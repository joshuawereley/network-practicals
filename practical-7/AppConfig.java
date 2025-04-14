import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AppConfig {

    private final Properties props = new Properties();
    private final String configFile = "pop3client.properties";

    public AppConfig() {
        try (InputStream input = new FileInputStream(configFile)) {
            props.load(input);
        } catch (IOException e) {
            props.setProperty("pop3.server", "pop.example.com");
            props.setProperty("pop3.port", "110");
        }
    }

    public String getServer() {
        return props.getProperty("pop3.server");
    }

    public int getPort() {
        return Integer.parseInt(props.getProperty("pop3.port"));
    }

    public String getUsername() {
        return props.getProperty("pop3.username");
    }

    public String getPassword() {
        return props.getProperty("pop3.password");
    }

    public void saveConfig(
        String server,
        int port,
        String username,
        String password
    ) throws IOException {
        props.setProperty("pop3.server", server);
        props.setProperty("pop3.port", String.valueOf(port));
        props.setProperty("pop3.username", username);
        props.setProperty("pop3.password", password);
        try (OutputStream output = new FileOutputStream(configFile)) {
            props.store(output, "POP3 Client Configuration");
        }
    }
}
