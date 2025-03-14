import java.net.Socket;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ContactManager contactManager;

  public ClientHandler(Socket socket, ContactManager contactManager) {
    this.socket = socket;
    this.contactManager = contactManager;
  }


}
