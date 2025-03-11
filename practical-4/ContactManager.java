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

  public boolean deleteContact(String name) {
    if (!name.isEmpty() && contacts.containsKey(name)) {
      contacts.remove(name);
      return true;
    }
    return false;
  }

  public String getAllContacts() {
    if (!contacts.isEmpty()) {
      StringBuilder response = new StringBuilder();
      response.append("<h2>Contact List</h2>");
      response.append("<table border='1'>");
      response.append("<tr><th>Name</th><th>Phone Number</th></tr>");

      for (String i : contacts.keySet()) {
        response.append("<tr>");
        response.append("<td>").append(i).append("</td>");
        response.append("<td>").append(contacts.get(i)).append("</td>");
        response.append("</tr>");
      }

      response.append("</table>");
      return response.toString();
    }
    return "<h3>No contacts available.</h3>";
  }
}
