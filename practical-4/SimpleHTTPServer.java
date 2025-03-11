import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHTTPServer {
  
  private static final int PORT 8080;
  private static ContactManager contactManager = new ContactManager();

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Server is running on port " + PORT);
      while (true) {
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(clientSocket);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
