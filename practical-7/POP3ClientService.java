public class POP3ClientService {
  private Socket socket;
  private BufferedReader input;
  private OutputStreamWriter output;

  public POP3ClientService(String server, int port) {
    if (!server.isEmpty()) {
      this.socket = new Socket(server, port);
      InputStreamReader isr = new InputStreamReader(socket.getInputStream());
      this.input = new BufferedReader(isr);
      this.output = new OutputStreamWriter(socket.getOutputStream());
      readServerResponse();
    }
  }

  public String readServerResponse() {
    return intput.readLine();
  }

  public boolean login(String username, String password) {
    if (!username.isEmpty() && !password.isEmpty()) {
      sendCommand("USER " + username);
      String response = readServerResponse();
      if (response.startsWith("+OK")) {
        sendCommand("PASS " + password);
        return true;
      }
      return false;
    }
  }

  public void sendCommand(String command) {
    output.write(command + "\r\n");
    output.flush();
  }
}
