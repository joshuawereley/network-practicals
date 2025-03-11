import java.util.HashMap;

public class ContactManager {

  private HashMap<String, String> contacts;

  public ContactManager() {
    contacts = new HashMap<String, String>();
  }

  public void addContact(String name, String number) {
    if (!name.isEmpty() && !number.isEmpty())
      contacts.put(name, number);
  }

  public String getContact(String name) {
    if (!name.isEmpty() && contacts.containsKey(name))
      return contacts.get(name);
    return null;
}
