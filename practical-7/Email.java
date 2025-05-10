import javax.swing.JCheckBox;

public class Email {

    private int messageNumber;
    private String from;
    private String subject;
    private int size;
    private JCheckBox checkBox;

    public Email(int messageNumber, String from, String subject, int size) {
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

    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

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
