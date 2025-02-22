import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TelnetServer {

    public TelnetServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                InetAddress address = socket.getInetAddress();
                System.out.println("Client connected: " + address);

                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TelnetServer ts = new TelnetServer(23);
    }
}
