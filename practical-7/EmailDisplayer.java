import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmailDisplayer {

    public static List<Integer> displayAndGetSelections(
        List<EmailInfo> emails,
        Scanner scanner
    ) {
        if (emails.isEmpty()) {
            System.out.println("\nNo messages found in your inbox.");
            return new ArrayList<>();
        }

        System.out.println("\nMessages in your inbox:");
        printTableHeader();

        for (EmailInfo email : emails) {
            printEmailRow(email);
        }

        return getDeletionSelections(emails, scanner);
    }

    private static void printTableHeader() {
        System.out.println(
            "+-----+--------------------------------+--------------------------------------+------------+"
        );
        System.out.println(
            "| No. | From                           | Subject                              | Size (bytes)|"
        );
        System.out.println(
            "+-----+--------------------------------+--------------------------------------+------------+"
        );
    }

    private static void printEmailRow(EmailInfo email) {
        String from = formatField(email.getFrom(), 30);
        String subject = formatField(email.getSubject(), 36);
        System.out.printf(
            "| %-3d | %-30s | %-36s | %-10d |\n",
            email.getMessageNumber(),
            from,
            subject,
            email.getSize()
        );
    }

    private static String formatField(String value, int maxLength) {
        if (value == null || value.equals("Unknown")) {
            return "Unknown";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private static List<Integer> getDeletionSelections(
        List<EmailInfo> emails,
        Scanner scanner
    ) {
        System.out.println(
            "+-----+--------------------------------+--------------------------------------+------------+"
        );
        System.out.println(
            "\nEnter the numbers of messages you want to delete (comma separated, e.g. 1,3,5)"
        );
        System.out.print("Or press Enter to skip deletion: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> selections = new ArrayList<>();
        String[] parts = input.split(",");
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part.trim());
                if (num > 0 && num <= emails.size()) {
                    selections.add(num);
                } else {
                    System.err.println("Invalid message number: " + num);
                }
            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid number: " + part);
            }
        }

        return selections;
    }
}
