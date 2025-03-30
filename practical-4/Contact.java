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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }
}
