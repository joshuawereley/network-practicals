import java.util.HashMap;

public class ContactManager {

  private HashMap<String, String> contacts;

  public ContactManager() {
    contacts = new HashMap<String, String>();
  }

  public void addContact(String name, String number) {
    contacts.put(name, number);
  }
}
