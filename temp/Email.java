import javax.swing.JCheckBox;

/**
 * Class representing an email message with its metadata
 */
public class Email {

    private int messageNumber;
    private String from;
    private String subject;
    private int size;
    private JCheckBox checkBox;

    /**
     * Constructor to create new Email object
     * @param messageNumber Message number on the server
     * @param from Sender email address
     * @param subject Email subject
     * @param size Size of the email in bytes
     */
    public Email(int messageNumber, String from, String subject, int size) {
        this.messageNumber = messageNumber;
        this.from = from;
        this.subject = subject;
        this.size = size;
    }

    /**
     * Get the message number
     * @return message number
     */
    public int getMessageNumber() {
        return messageNumber;
    }

    /**
     * Get the sender email address
     * @return sender email
     */
    public String getFrom() {
        return from;
    }

    /**
     * Get the email subject
     * @return email subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get the email size
     * @return size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the checkbox associated with this email
     * @param checkBox JCheckBox reference
     */
    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

    /**
     * Check if the email is selected for deletion
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return checkBox != null && checkBox.isSelected();
    }

    @Override
    public String toString() {
        return (
            "From: " +
            from +
            ", Subject: " +
            subject +
            ", Size: " +
            size +
            " bytes"
        );
    }
}
