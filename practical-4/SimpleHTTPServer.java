import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHTTPServer {

  private static final int PORT = 8081;

  public static void main(String[] args) {
    ContactManager contactManager = new ContactManager();

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Server started on http://localhost:" + PORT);
      System.out.println("Press Ctrl+C to stop the server");

      while (true) {
        Socket socket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(socket, contactManager);
        Thread thread = new Thread(clientHandler);
        thread.start();
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }
}
