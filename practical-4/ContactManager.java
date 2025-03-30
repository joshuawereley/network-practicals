import java.util.HashMap;
import java.util.Base64;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

public class ContactManager {

  private HashMap<String, String> contacts;
  private HashMap<String, byte[]> contactImages;

  public ContactManager() {
    contacts = new HashMap<String, String>();
    contactImages = new HashMap<String, byte[]>();
  }

  public void addContact(String name, String number) {
    if (!name.isEmpty() && !number.isEmpty()) {
      contacts.put(name, number);
    }
  }

  public void addContactImage(String name, byte[] imageData) {
    if (!name.isEmpty() && imageData != null) {
      contactImages.put(name, imageData);
    }
  }

  public String getContact(String name) {
    if (!name.isEmpty() && contacts.containsKey(name)) {
      return contacts.get(name);
    }
    return null;
  }

  public byte[] getContactImage(String name) {
    if (!name.isEmpty()) {
      return contactImages.get(name);
    }
    return null;
  }

  public boolean deleteContact(String name) {
    if (!name.isEmpty() && contacts.containsKey(name)) {
      contacts.remove(name);
      contactImages.remove(name);
      return true;
    }
    return false;
  }

  public String getAllContacts() {
    StringBuilder response = new StringBuilder();
    if (contact.isEmpty()) {
      response.append("<p>No contacts available.</p>");
    } else {
      response.append("<table border='1' style='width:100%'>");
      response.append("<tr><th>Name</th><th>Phone No.</th><th>Action</th></tr>");

      for (String i : contacts.keySet()) {
        response.append("<tr>");
        response.append("<td>").append(escapeHtml(i)).append("</td>");
        response.append("<td>").append(escapeHtml(contacts.get(i))).append("</td>");
        response.append("<td><a href='/deleteContact?name=");
        response.append(encodeUrl(i)).append("'>Delete</a>");
        response.append(" | <a href'/image?name=").append(encodeUrl(i));
        response.append("'>View Image</a></td>");
        response.append("</tr>");
      }
      response.append("</table>");
      return response.toString();
    }
  }

  public String escapeHtml(String input) {
    return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
  }

  public String encodeUrl(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return input;
    }
  }
}
