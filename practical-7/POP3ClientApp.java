public class POP3ClientApp {
  private String server;
  private String port;
  private String username;
  private String password;
  private POP3ClientService pop3ClientService;
  private EmailListView emailListView;

  public POP3ClientApp(String server, String port, String username, String password) {
    if (!server.isEmpty() && !port.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
      this.server = server;
      this.port = port;
      this.username = username;
      this.password = password;
      pop3ClientService = new POP3ClientService(this.server, this.port);
      emailListView = new EmailListView();
    }
  }


}
