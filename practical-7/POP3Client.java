import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class POP3Client {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String server;
    private int port;
    private boolean connected = false;

    public POP3Client(String server, int port) {
        this.server = server;
        this.port = port;
    }

    private void connect() throws IOException {
        if (connected) {
            return;
        }

        socket = new Socket(server, port);

        reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        writer = new PrintWriter(socket.getOutputStream(), true);

        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException(
                "Failed to connect to POP3 server: " + response
            );
        }

        connected = true;
    }

    public boolean login(String username, String password) throws IOException {
        connect();

        writer.println("USER " + username);
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            return false;
        }

        writer.println("PASS " + password);
        response = reader.readLine();

        return response.startsWith("+OK");
    }

    public ArrayList<Email> listEmails() throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        ArrayList<Email> emails = new ArrayList<>();

        writer.println("STAT");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException("Failed to get mailbox status: " + response);
        }

        String[] parts = response.split(" ");
        int messageCount = Integer.parseInt(parts[1]);

        for (int i = 1; i <= messageCount; i++) {
            writer.println("LIST " + i);
            response = reader.readLine();

            if (!response.startsWith("+OK")) {
                continue;
            }

            parts = response.split(" ");
            int size = Integer.parseInt(parts[2]);

            writer.println("TOP " + i + " 0");
            response = reader.readLine();

            if (!response.startsWith("+OK")) {
                continue;
            }

            String from = "Unknown";
            String subject = "No Subject";

            String line;
            while ((line = reader.readLine()) != null && !line.equals(".")) {
                if (line.toLowerCase().startsWith("from:")) {
                    from = line.substring(5).trim();
                } else if (line.toLowerCase().startsWith("subject:")) {
                    subject = line.substring(8).trim();
                }
            }

            emails.add(new Email(i, from, subject, size));
        }

        return emails;
    }

    public void deleteEmail(int messageNumber) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        writer.println("DELE " + messageNumber);
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException(
                "Failed to delete message " + messageNumber + ": " + response
            );
        }
    }

    public void quit() throws IOException {
        if (!connected) {
            return;
        }

        writer.println("QUIT");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            System.err.println(
                "Warning: Server didn't acknowledge QUIT command: " + response
            );
        } else {
            System.out.println("Server confirmed QUIT: " + response);
        }

        reader.close();
        writer.close();
        socket.close();

        connected = false;
    }

    public void reset() throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        writer.println("RSET");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException("Failed to reset session: " + response);
        }
    }
}
