import java.util

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

  public void connectAndFetchEmails() {
    try {
      String status = pop3ClientService.login(username, password);
      if (status == "SUCCESS") {
        List<String> emailList = pop3ClientService.fetchEmailMetaData();
        emailListView.displayEmails(emailList);
        List<String> markedForDeletion = emailListView.getMarkedEmails();
        if (markedForDeletion != null) {
          pop3ClientService.deleteEmails(markedForDeletion);
          emailListView.showMessage("Emails deleted successfully.");
        } else {
          emailListView.showMessage("No emails selected.");
        }
      } else {
        emailListView.showError("Login failed. Check credentials.");
      }
    } catch (NetworkException e) {
      emailListView.showError("Connection error: " + e.message())
    } finally {
      pop3ClientService.disconnect();
    }
  }


}
