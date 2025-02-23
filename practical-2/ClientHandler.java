import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(
        ClientHandler.class.getName()
    );
    private final Socket socket;
    private final CommandParser parser;
    private final ResponseFormatter formatter;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.formatter = new ResponseFormatter(new ANSIFormatter());
        this.parser = new CommandParser(new FriendDatabase());
    }

    @Override
    public void run() {
        try (
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true)
        ) {
            output.println(
                formatter.formatResponse(
                    "Welcome to the Telnet server!",
                    Constants.COLOR_SUCCESS
                )
            );
            output.println("Type HELP for available commands.");

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("EXIT")) {
                    output.println(
                        formatter.formatResponse(
                            "Goodbye!",
                            Constants.COLOR_ERROR
                        )
                    );
                    break;
                }
                output.println(parser.processCommand(clientMessage));
            }
        } catch (IOException e) {
            logger.severe("Client disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.severe("Error closing socket: " + e.getMessage());
            }
        }
    }
}
