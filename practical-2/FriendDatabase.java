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

    private static final Logger logger = Logger.getLogger(
        FriendDatabase.class.getName()
    );
    private static final String FILE_NAME = "friends.txt";
    private final HashMap<String, String> storage;

    public FriendDatabase() {
        storage = new HashMap<String, String>();
        loadContacts();
    }

    public synchronized void loadContacts() {
        try (Scanner reader = new Scanner(new File(FILE_NAME))) {
            while (reader.hasNextLine()) {
                String[] splitData = reader.nextLine().split(",");
                if (splitData.length == 3) {
                    String fullName =
                        splitData[0].trim() + " " + splitData[1].trim();
                    storage.put(fullName, splitData[2].trim());
                }
            }
        } catch (IOException e) {
            logger.severe("Error loading database: " + e.getMessage());
        }
    }

    public synchronized void saveContacts() {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME)) {
            for (String fullName : storage.keySet()) {
                String[] nameParts = fullName.split(" ", 2);
                if (nameParts.length == 2) {
                    fileWriter.write(
                        nameParts[0] +
                        "," +
                        nameParts[1] +
                        "," +
                        storage.get(fullName) +
                        "\n"
                    );
                } else {
                    logger.warning(
                        "Invalid name format in database: " + fullName
                    );
                }
            }
        } catch (IOException e) {
            logger.severe("Error saving database: " + e.getMessage());
        }
    }

    public synchronized String addContact(
        String name,
        String surname,
        String phoneNumber
    ) {
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
        storage.forEach((key, value) ->
            list.append(key).append(": ").append(value).append("\n")
        );
        return list.toString();
    }

    public synchronized void createBackup() {
        try {
            Files.copy(
                Paths.get(FILE_NAME),
                Paths.get(FILE_NAME + ".backup_" + System.currentTimeMillis()),
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            logger.severe("Error creating backup: " + e.getMessage());
        }
    }
}
