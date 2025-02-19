import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket s;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(s.getInputStream());
            input = new BufferedReader(isr);
            output = new PrintWriter(s.getOutputStream(), true);

            output.println("Hello! Welcome to the Telnet server!");
            output.println("Type HELP for a list of commands.");

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("EXIT")) {
                    output.println("Goodbye");
                    break;
                } else if (clientMessage.equalsIgnoreCase("HELP")) {
                    output.println(
                        "Available commands: ADD, SEARCH, DELETE, LIST, EXIT"
                    );
                } else {
                    output.println("Unknown command. Type HELP for options.");
                }
            }
        } catch (IOException i) {
            System.out.println("Error handling client: " + i.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                if (s != null) {
                    s.close();
                }
            } catch (IOException i) {
                System.out.println(
                    "Error closing resources: " + i.getMessage()
                );
            }
        }
    }
}
