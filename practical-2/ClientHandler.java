import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private CommandParser parser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        parser = new CommandParser(new FriendDatabase());
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(
                socket.getInputStream()
            );
            input = new BufferedReader(isr);
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Welcome to the Telnet server!");
            output.println("Type HELP for available commands.");

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("EXIT")) {
                    output.println("Goodbye!");
                    break;
                }
                String response = parser.processCommand(clientMessage);
                output.println(response);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client: " + e.getMessage());
            }
        }
    }
}
