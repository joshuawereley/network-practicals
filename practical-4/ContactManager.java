import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;

public class ContactManager {

    private HashMap<String, String> contacts;
    private HashMap<String, byte[]> contactImages;

    public ContactManager() {
        contacts = new HashMap<>();
        contactImages = new HashMap<>();

        // Add some sample data
        contacts.put("John Doe", "123-456-7890");
        contacts.put("Jane Smith", "987-654-3210");
    }

    public void addContact(String name, String number) {
        if (!name.isEmpty() && !number.isEmpty()) {
            contacts.put(name, number);
        }
    }

    public String getContact(String name) {
        return contacts.get(name);
    }

    public boolean deleteContact(String name) {
        if (contacts.containsKey(name)) {
            contacts.remove(name);
            contactImages.remove(name); // Also remove associated image
            return true;
        }
        return false;
    }

    public void addContactImage(String name, byte[] imageData) {
        if (!name.isEmpty() && imageData != null) {
            contactImages.put(name, imageData);
        }
    }

    public byte[] getContactImage(String name) {
        return contactImages.get(name);
    }

    public String getAllContactsHTML() {
        StringBuilder sb = new StringBuilder();

        if (contacts.isEmpty()) {
            sb.append("<p>No contacts available.</p>");
        } else {
            sb.append("<table border='1' style='width:100%'>");
            sb.append("<tr><th>Name</th><th>Phone</th><th>Action</th></tr>");

            for (String name : contacts.keySet()) {
                sb.append("<tr>");
                sb.append("<td>").append(escapeHtml(name)).append("</td>");
                sb
                    .append("<td>")
                    .append(escapeHtml(contacts.get(name)))
                    .append("</td>");
                sb
                    .append("<td><a href='/deleteContact?name=")
                    .append(encodeUrl(name))
                    .append("'>Delete</a>");
                sb
                    .append(" | <a href='/image?name=")
                    .append(encodeUrl(name))
                    .append("'>View Image</a></td>");
                sb.append("</tr>");
            }

            sb.append("</table>");
        }

        return sb.toString();
    }

    private String escapeHtml(String input) {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String encodeUrl(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return input; // fallback
        }
    }
}
