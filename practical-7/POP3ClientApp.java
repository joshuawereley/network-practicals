import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class POP3ClientApp {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        POP3ClientService pop3Service = new POP3ClientService();
        EmailListView view = new EmailListView();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("POP3 Server [" + config.getServer() + "]: ");
            String server = scanner.nextLine();
            if (server.isEmpty()) server = config.getServer();

            System.out.print("Port [" + config.getPort() + "]: ");
            String portInput = scanner.nextLine();
            int port = portInput.isEmpty()
                ? config.getPort()
                : Integer.parseInt(portInput);

            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (
                !server.equals(config.getServer()) || port != config.getPort()
            ) {
                config.saveConfig(server, port, username, password);
            }

            pop3Service.connect(server, port);
            if (!pop3Service.login(username, password)) {
                view.showError("Login failed. Check credentials.");
                return;
            }

            List<EmailMetaData> emails = pop3Service.fetchEmailMetadata();
            view.displayEmails(emails);

            List<Integer> toDelete = view.getEmailsToDelete();
            if (!toDelete.isEmpty()) {
                pop3Service.deleteEmails(toDelete);
                view.showMessage(
                    toDelete.size() + " emails deleted successfully."
                );
            }
        } catch (Exception e) {
            view.showError("Error: " + e.getMessage());
        } finally {
            try {
                pop3Service.disconnect();
            } catch (IOException e) {
                view.showError("Error disconnecting: " + e.getMessage());
            }
            scanner.close();
        }
    }
}
