import java.util.Hashmap;

public class Contact {
  private String name;
  private String surname;
  private String number;
  private byte[] image;

  public Contact(String name, String surname, String number, byte[] image) {
    this.name = name;
    this.surname = surname;
    this.number = number;
    this.image = image;
  }
}
