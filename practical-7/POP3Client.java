import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class POP3Client {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Gmail POP3 Client");
        System.out.print("Enter your Gmail address: ");
        String username = scanner.nextLine();

        System.out.print(
            "Enter your Gmail password (or app password if 2FA is enabled): "
        );
        String password = scanner.nextLine();

        POP3Connection connection = new POP3Connection();

        try {
            connection.connect();
            connection.login(username, password);

            List<EmailInfo> emails = connection.listEmails();

            if (emails.isEmpty()) {
                System.out.println("\nNo messages found in your inbox.");
            } else {
                List<Integer> emailsToDelete =
                    EmailDisplayer.displayAndGetSelections(emails, scanner);

                if (!emailsToDelete.isEmpty()) {
                    System.out.println("\nDeleting selected emails...");
                    for (int msgNum : emailsToDelete) {
                        connection.deleteEmail(msgNum);
                    }
                    System.out.println("Deletion completed.");
                }
            }

            connection.quit();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
