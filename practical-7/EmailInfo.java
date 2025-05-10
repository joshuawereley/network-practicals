public class EmailInfo {

    private final int messageNumber;
    private final String from;
    private final String subject;
    private final int size;

    public EmailInfo(int messageNumber, String from, String subject, int size) {
        this.messageNumber = messageNumber;
        this.from = from;
        this.subject = subject;
        this.size = size;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public int getSize() {
        return size;
    }
}
