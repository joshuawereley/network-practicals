import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public class FriendDatabase {

    private static final Logger logger = Logger.getLogger(FriendDatabase.class.getName());
    private static final String FILE_NAME = "friends.txt";
    private final HashMap<String, String> storage;
    private final HashMap<String, String> users;
    private static final String BACKUP_FOLDER = "Backup";

    public FriendDatabase() {
        storage = new HashMap<>();
        users = new HashMap<>();
        loadContacts();
    }

    public synchronized void loadContacts() {
        try (Scanner reader = new Scanner(new File(FILE_NAME))) {
            while (reader.hasNextLine()) {
                String[] contact = reader.nextLine().split(",");
                if (contact.length == 3) {
                    storage.put(contact[0] + " " + contact[1], contact[2]);
                }
            }
        } catch (IOException e) {
            logger.severe("Error loading database: " + e.getMessage());
        }
    }

    public synchronized void saveContacts() {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME)) {
            for (String fullName : storage.keySet()) {
                String[] nameParts = fullName.split(" ");
                fileWriter.write(nameParts[0] + "," + nameParts[1] + "," + storage.get(fullName) + "\n");
            }
        } catch (IOException e) {
            logger.severe("Error saving database: " + e.getMessage());
        }
    }

    public synchronized String addContact(String name, String surname, String phoneNumber) {
        createBackup();
        String fullName = name + " " + surname;
        if (storage.containsKey(fullName)) {
            return fullName + " already exists!";
        }
        storage.put(fullName, phoneNumber);
        saveContacts();
        return fullName + " added successfully!";
    }

    public synchronized String searchContact(String name, String surname) {
        String fullName = name + " " + surname;
        return storage.getOrDefault(fullName, fullName + " not found!");
    }

    public synchronized String deleteContact(String name, String surname) {
        createBackup();
        String fullName = name + " " + surname;
        if (storage.remove(fullName) != null) {
            saveContacts();
            return fullName + " deleted successfully!";
        }
        return fullName + " not found!";
    }

    public synchronized String listContacts() {
        if (storage.isEmpty()) return "No contacts available!";
        StringBuilder list = new StringBuilder();
        storage.forEach((key, value) -> list.append(key).append(": ").append(value).append("\n"));
        return list.toString();
    }

    public synchronized void createBackup() {
        try {
            Files.createDirectories(Paths.get(BACKUP_FOLDER));
            Files.copy(Paths.get(FILE_NAME), Paths.get(BACKUP_FOLDER, FILE_NAME + "_" + System.currentTimeMillis()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.severe("Error creating backup: " + e.getMessage());
        }
    }

    public synchronized String registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return "Username already exists!";
        }
        users.put(username, password);
        saveContacts();
        return "User registered successfully!";
    }

    public synchronized String loginUser(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            return "Login successful!";
        }
        return "Invalid username or password!";
    }
}
