public class EmailMetaData {
  private int id;
  private String sender;
  private String subject;
  private int size;

  public EmailMetaData(int id, String sender, String subject, int size) {
    if (!sender.isEmpty() && !subject.isEmpty()) {
      this.id = id;
      this.sender = sender;
      this.subject = subject;
      this.size = size;
    }
  }

  public int getID() {
    return id;
  }

  public String getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }

  public int getSize() {
    return size;
  }
}
