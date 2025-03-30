import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContactManager {

    private final Map<Integer, Contact> contacts;
    private int nextId = 1;
    private Timer autoSaveTimer;
    private static final String DATA_FILE = "contacts.dat";

    public ContactManager() {
        this.contacts = new ConcurrentHashMap<>();
        initializeFromDisk();
        startAutoSave();
    }

    private void initializeFromDisk() {
        try {
            Map<Integer, Contact> loadedContacts = loadContactsFromDisk();
            contacts.putAll(loadedContacts);
            nextId = contacts.isEmpty()
                ? 1
                : Collections.max(contacts.keySet()) + 1;
            System.out.println(
                "[Persistence] Loaded " + contacts.size() + " contacts"
            );
        } catch (Exception e) {
            System.err.println(
                "[Persistence] Failed to load contacts: " + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Contact> loadContactsFromDisk()
        throws IOException, ClassNotFoundException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (
            ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file))
            )
        ) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<Integer, Contact>) obj;
            }
            throw new IOException("Invalid data format in contacts file");
        }
    }

    private void startAutoSave() {
        autoSaveTimer = new Timer(true);
        autoSaveTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    saveContactsToDisk();
                }
            },
            300000,
            300000
        ); // Auto-save every 5 minutes
    }

    public synchronized int addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }

        int newId = nextId++;
        contacts.put(newId, contact);
        saveContactsToDisk();
        return newId;
    }

    public synchronized boolean deleteContact(int id) {
        boolean removed = contacts.remove(id) != null;
        if (removed) {
            saveContactsToDisk();
        }
        return removed;
    }

    public synchronized boolean updateContact(int id, Contact updatedContact) {
        if (!contacts.containsKey(id)) {
            return false;
        }
        contacts.put(id, updatedContact);
        saveContactsToDisk();
        return true;
    }

    public Contact getContact(int id) {
        return contacts.get(id);
    }

    public List<Map.Entry<Integer, Contact>> searchContacts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerQuery = query.toLowerCase();
        List<Map.Entry<Integer, Contact>> results = new ArrayList<>();

        for (Map.Entry<Integer, Contact> entry : contacts.entrySet()) {
            Contact contact = entry.getValue();
            if (
                String.valueOf(entry.getKey()).contains(lowerQuery) ||
                contact.getName().toLowerCase().contains(lowerQuery) ||
                contact.getSurname().toLowerCase().contains(lowerQuery) ||
                contact.getNumber().toLowerCase().contains(lowerQuery)
            ) {
                results.add(entry);
            }
        }
        return results;
    }

    public String getAllContactsHTML() {
        if (contacts.isEmpty()) {
            return "<p>No contacts found</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append(
            "<table border='1'><tr><th>ID</th><th>Name</th><th>Surname</th><th>Phone</th><th>Image</th><th>Actions</th></tr>"
        );

        contacts
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                int id = entry.getKey();
                Contact contact = entry.getValue();

                html
                    .append("<tr>")
                    .append("<td>")
                    .append(id)
                    .append("</td>")
                    .append("<td>")
                    .append(escapeHtml(contact.getName()))
                    .append("</td>")
                    .append("<td>")
                    .append(escapeHtml(contact.getSurname()))
                    .append("</td>")
                    .append("<td>")
                    .append(escapeHtml(contact.getNumber()))
                    .append("</td>")
                    .append("<td>")
                    .append(
                        contact.getImage() != null
                            ? "<img src='/image?id=" +
                            id +
                            "' style='max-width:100px'>"
                            : "No image"
                    )
                    .append("</td>")
                    .append("<td>")
                    .append("<a href='/view?id=")
                    .append(id)
                    .append("' class='view-btn'>View</a> ")
                    .append("<a href='/edit?id=")
                    .append(id)
                    .append("' class='edit-btn'>Edit</a> ")
                    .append("<a href='/delete?id=")
                    .append(id)
                    .append("' class='delete-btn'>Delete</a>")
                    .append("</td>")
                    .append("</tr>");
            });

        html.append("</table>");
        return html.toString();
    }

    private synchronized void saveContactsToDisk() {
        try {
            Map<Integer, Contact> saveCopy = new HashMap<>(contacts);
            ContactPersistence.saveContacts(saveCopy);
            System.out.println(
                "[Persistence] Successfully saved " +
                contacts.size() +
                " contacts"
            );
        } catch (IOException e) {
            System.err.println(
                "[Persistence] Failed to save contacts: " + e.getMessage()
            );
        }
    }

    public void shutdown() {
        autoSaveTimer.cancel();
        saveContactsToDisk();
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private static class ContactPersistence {

        static void saveContacts(Map<Integer, Contact> contacts)
            throws IOException {
            try (
                ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(DATA_FILE))
                )
            ) {
                oos.writeObject(contacts);
            }
        }
    }
}
