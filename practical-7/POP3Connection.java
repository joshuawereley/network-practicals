import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class POP3Connection {

    private static final String POP3_SERVER = "pop.gmail.com";
    private static final int POP3_PORT = 995;
    private static final int TIMEOUT = 15000;
    private boolean debug = true;

    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;

    public void connect() throws IOException {
        SSLSocketFactory factory =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) factory.createSocket(POP3_SERVER, POP3_PORT);
        socket.setSoTimeout(TIMEOUT);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream()),
            true
        );

        String response = readResponse();
        if (debug) System.out.println("SERVER GREETING: " + response);
        if (!response.startsWith("+OK")) {
            throw new IOException("POP3 server error: " + response);
        }
    }

    public void login(String username, String password) throws IOException {
        sendCommand("USER " + username);
        sendCommand("PASS " + password);
    }

    public List<EmailInfo> listEmails() throws IOException {
        List<EmailInfo> emails = new ArrayList<>();

        // First get STAT to verify messages exist
        String statResponse = sendCommand("STAT");
        if (debug) System.out.println("STAT RESPONSE: " + statResponse);

        if (!statResponse.startsWith("+OK")) {
            throw new IOException("STAT command failed: " + statResponse);
        }

        String[] statParts = statResponse.split("\\s+");
        int messageCount = Integer.parseInt(statParts[1]);
        if (messageCount == 0) {
            return emails;
        }

        // Get individual message sizes
        String listResponse = sendCommand("LIST");
        if (debug) System.out.println("LIST RESPONSE: " + listResponse);

        // Parse message numbers and sizes
        String[] listLines = listResponse.split("\r?\n");
        for (String line : listLines) {
            if (line.matches("^\\d+\\s+\\d+$")) {
                String[] parts = line.split("\\s+");
                int msgNum = Integer.parseInt(parts[0]);
                int size = Integer.parseInt(parts[1]);

                try {
                    // Get headers
                    String headers = sendCommand("TOP " + msgNum + " 30");
                    if (debug) System.out.println(
                        "HEADERS FOR MSG " + msgNum + ":\n" + headers
                    );

                    // Parse headers
                    String from = extractHeader(headers, "From:");
                    String subject = extractHeader(headers, "Subject:");

                    emails.add(
                        new EmailInfo(
                            msgNum,
                            cleanHeader(from),
                            cleanHeader(subject),
                            size
                        )
                    );
                } catch (Exception e) {
                    System.err.println(
                        "Error processing message " +
                        msgNum +
                        ": " +
                        e.getMessage()
                    );
                    emails.add(new EmailInfo(msgNum, "Error", "Error", size));
                }
            }
        }

        return emails;
    }

    private String extractHeader(String headers, String headerName) {
        // Handle multi-line headers
        Pattern pattern = Pattern.compile(
            "^" + headerName + "(.*?)(?=\\r?\\n\\S|$)",
            Pattern.MULTILINE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(headers);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String cleanHeader(String header) {
        if (header == null || header.isEmpty()) {
            return "Unknown";
        }
        // Remove quotes and extra spaces
        header = header
            .replaceAll("\"", "")
            .replaceAll("\\r?\\n\\s+", " ")
            .trim();
        return header.isEmpty() ? "Unknown" : header;
    }

    public void deleteEmail(int msgNum) throws IOException {
        sendCommand("DELE " + msgNum);
    }

    public void quit() throws IOException {
        try {
            sendCommand("QUIT");
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    private String sendCommand(String command) throws IOException {
        if (debug) System.out.println("CLIENT: " + command);
        out.println(command);
        return readResponse();
    }

    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
            if (line.equals(".")) break;
            if (line.startsWith("+OK") && !line.contains("\r\n")) break;
            if (line.startsWith("-ERR")) break;
        }

        return response.toString();
    }
}
