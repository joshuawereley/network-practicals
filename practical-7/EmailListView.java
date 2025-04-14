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
        for (EmailMetadata email : emails) {
            System.out.printf(
                "%-3d | %-12d | %-20s | %s%n",
                email.getId(),
                email.getSize(),
                truncate(email.getSender(), 20),
                truncate(email.getSubject(), 20)
            );
        }
        System.out.println();
    }
}
