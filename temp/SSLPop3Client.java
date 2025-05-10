import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.net.ssl.*;

/**
 * Extension of POP3Client that uses SSL for secure connections
 * This is needed for Gmail and other providers that require encryption
 */
public class SSLPop3Client extends POP3Client {

    private SSLSocket sslSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String server;
    private int port;
    private boolean connected = false;

    /**
     * Constructor that initializes server and port
     * @param server The POP3 server hostname
     * @param port The POP3 server port
     */
    public SSLPop3Client(String server, int port) {
        super(server, port);
        this.server = server;
        this.port = port;
    }

    /**
     * Connect to the POP3 server using SSL
     * @throws IOException If connection fails
     */
    private void connect() throws IOException {
        if (connected) {
            return;
        }

        try {
            // Create SSL socket factory
            SSLSocketFactory sslSocketFactory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

            // Create socket connection with SSL
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(server, port);

            // Setup input and output streams
            reader = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream())
            );
            writer = new PrintWriter(sslSocket.getOutputStream(), true);

            // Read server greeting
            String response = reader.readLine();

            if (!response.startsWith("+OK")) {
                throw new IOException(
                    "Failed to connect to POP3 server: " + response
                );
            }

            connected = true;
        } catch (Exception e) {
            throw new IOException("SSL connection failed: " + e.getMessage());
        }
    }

    /**
     * Login to the POP3 server using USER and PASS commands
     * @param username The username
     * @param password The password
     * @return true if login successful, false otherwise
     * @throws IOException If communication fails
     */
    @Override
    public boolean login(String username, String password) throws IOException {
        connect();

        // Send USER command
        writer.println("USER " + username);
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            return false;
        }

        // Send PASS command
        writer.println("PASS " + password);
        response = reader.readLine();

        return response.startsWith("+OK");
    }

    /**
     * Get the list of all emails in the mailbox
     * @return ArrayList of Email objects
     * @throws IOException If communication fails
     */
    @Override
    public ArrayList<Email> listEmails() throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        ArrayList<Email> emails = new ArrayList<>();

        // Send STAT command to get mailbox stats
        writer.println("STAT");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException("Failed to get mailbox status: " + response);
        }

        // Parse response to get message count
        String[] parts = response.split(" ");
        int messageCount = Integer.parseInt(parts[1]);

        // Loop through all messages
        for (int i = 1; i <= messageCount; i++) {
            // Get message size
            writer.println("LIST " + i);
            response = reader.readLine();

            if (!response.startsWith("+OK")) {
                continue;
            }

            parts = response.split(" ");
            int size = Integer.parseInt(parts[2]);

            // Get message headers
            writer.println("TOP " + i + " 0");
            response = reader.readLine();

            if (!response.startsWith("+OK")) {
                continue;
            }

            String from = "Unknown";
            String subject = "No Subject";

            // Parse headers
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

    /**
     * Mark an email for deletion
     * @param messageNumber The message number to delete
     * @throws IOException If communication fails
     */
    @Override
    public void deleteEmail(int messageNumber) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        // Send DELE command
        writer.println("DELE " + messageNumber);
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException(
                "Failed to delete message " + messageNumber + ": " + response
            );
        }
    }

    /**
     * Quit the POP3 session and close the connection
     * This will also commit any deletions
     * @throws IOException If communication fails
     */
    @Override
    public void quit() throws IOException {
        if (!connected) {
            return;
        }

        // Send QUIT command
        writer.println("QUIT");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            System.err.println(
                "Warning: Server didn't acknowledge QUIT command: " + response
            );
        } else {
            System.out.println("Server confirmed QUIT: " + response);
        }

        // Close streams and socket
        reader.close();
        writer.close();
        sslSocket.close();

        connected = false;
    }

    /**
     * Reset the POP3 session, undoing any marked deletions
     * @throws IOException If communication fails
     */
    @Override
    public void reset() throws IOException {
        if (!connected) {
            throw new IOException("Not connected to POP3 server");
        }

        // Send RSET command
        writer.println("RSET");
        String response = reader.readLine();

        if (!response.startsWith("+OK")) {
            throw new IOException("Failed to reset session: " + response);
        }
    }
}
