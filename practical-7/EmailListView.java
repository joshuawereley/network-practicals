import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EmailListView {

    private final Scanner emailScanner;

    public EmailListView() {
        this.emailScanner = new Scanner(System.in);
    }

    public void displayEmails(List<EmailMetaData> emails) {
        System.out.println("\n=== Emails in Your Mailbox ===");
        System.out.println(
            "ID  | Size (bytes) | From                | Subject"
        );
        System.out.println(
            "----|--------------|---------------------|-------------------"
        );
        for (EmailMetaData email : emails) {
            System.out.printf(
                "%-3d | %-12d | %-20s | %s%n",
                email.getID(),
                email.getSize(),
                truncate(email.getSender(), 20),
                truncate(email.getSubject(), 20)
            );
        }
        System.out.println();
    }

    public List<Integer> getEmailsToDelete() {
        System.out.println(
            "Enter the IDs of emails you want to delete (comma-separated):"
        );
        System.out.println("> ");
        String input = emailScanner.nextLine().trim();
        return parseIDList(input);
    }

    public void showMessage(String message) {
        System.out.println("[INFO] " + message);
    }

    public void showError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public void close() {
        emailScanner.close();
    }

    public String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        return str.length() > maxLength
            ? str.substring(0, maxLength - 3) + "..."
            : str;
    }

    public List<Integer> parseIDList(String input) {
        return Arrays.stream(input.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}
