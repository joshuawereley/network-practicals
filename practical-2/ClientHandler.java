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
    private ANSIFormatter formatter;
    private UserAuthentication authenticator;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        formatter = new ANSIFormatter();
        authenticator = new UserAuthentication();
        parser = new CommandParser(new FriendDatabase(), authenticator);
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(
                socket.getInputStream()
            );
            input = new BufferedReader(isr);
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println(
                formatter.colourText("Welcome to the Telnet server!", "32")
            );
            output.println("Please log in.");

            output.print("Username: ");
            String username = input.readLine();
            output.print("\nPassword: ");
            String password = input.readLine();
            output.println("");

            if (authenticator.authenticate(username, password)) {
                output.println(formatter.colourText("Login successful!", "32"));
                output.println("Type HELP for available commands.");

                String clientMessage;
                while ((clientMessage = input.readLine()) != null) {
                    if (clientMessage.equalsIgnoreCase("EXIT")) {
                        output.println(formatter.colourText("Goodbye!", "31"));
                        break;
                    }
                    String response = parser.processCommand(clientMessage);
                    output.println(response);
                }
            } else {
                output.println(
                    formatter.colourText(
                        "Invalid username or password. Disconnecting...",
                        "31"
                    )
                );
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
