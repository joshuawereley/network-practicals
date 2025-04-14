import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EmailListView {

    private final Scanner scanner = new Scanner(System.in);

    public void displayEmails(List<EmailMetaData> emails) {
        System.out.println("\n=== Emails in Your Mailbox ===");
        System.out.printf(
            "%-5s %-10s %-30s %s%n",
            "ID",
            "Size",
            "From",
            "Subject"
        );
        emails.forEach(email ->
            System.out.printf(
                "%-5d %-10d %-30s %s%n",
                email.getID(),
                email.getSize(),
                truncate(email.getSender(), 30),
                truncate(email.getSubject(), 50)
            )
        );
    }

    public List<Integer> getEmailsToDelete() {
        System.out.print("\nEnter email IDs to delete (comma-separated): ");
        String input = scanner.nextLine();
        return Arrays.stream(input.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }

    public void showMessage(String message) {
        System.out.println("[INFO] " + message);
    }

    public void showError(String message) {
        System.err.println("[ERROR] " + message);
    }

    private String truncate(String str, int length) {
        return str != null && str.length() > length
            ? str.substring(0, length - 3) + "..."
            : str;
    }
}
