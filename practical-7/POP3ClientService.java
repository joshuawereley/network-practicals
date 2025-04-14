import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class POP3ClientService {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private final POP3ResponseParser parser = new POP3ResponseParser();

    public void connect(String server, int port) throws IOException {
        socket = new Socket(server, port);
        input = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        output = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream())
        );
        readResponse();
    }

    public boolean login(String username, String password) throws IOException {
        sendCommand("USER " + username);
        if (parser.isSuccess(readResponse())) {
            sendCommand("PASS " + password);
            return parser.isSuccess(readResponse());
        }
        return false;
    }

    public List<EmailMetaData> fetchEmailMetadata() throws IOException {
        List<EmailMetaData> emails = new ArrayList<>();
        sendCommand("LIST");
        Map<Integer, Integer> idToSize = parser.parseListResponse(
            readMultilineResponse()
        );

        for (Map.Entry<Integer, Integer> entry : idToSize.entrySet()) {
            sendCommand("TOP " + entry.getKey() + " 0");
            List<String> headers = readMultilineResponse();
            String sender = parser.extractHeader(headers, "From:");
            String subject = parser.extractHeader(headers, "Subject:");
            emails.add(
                new EmailMetaData(
                    entry.getKey(),
                    sender,
                    subject,
                    entry.getValue()
                )
            );
        }
        return emails;
    }

    public void deleteEmails(List<Integer> ids) throws IOException {
        for (int id : ids) {
            sendCommand("DELE " + id);
            readResponse();
        }
    }

    public void disconnect() throws IOException {
        sendCommand("QUIT");
        socket.close();
    }

    private void sendCommand(String command) throws IOException {
        output.write(command + "\r\n");
        output.flush();
    }

    private String readResponse() throws IOException {
        return input.readLine();
    }

    private List<String> readMultilineResponse() throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = input.readLine()) != null && !line.equals(".")) {
            lines.add(line);
        }
        return lines;
    }
}
