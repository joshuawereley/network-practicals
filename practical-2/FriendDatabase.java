import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Scanner;

public class FriendDatabase {

    private HashMap<String, String> storage;
    private static final String FILE_NAME = "friends.txt";

    public FriendDatabase() {
        storage = new HashMap<String, String>();
        loadContacts();
    }

    public synchronized void loadContacts() {
        try {
            File file = new File(FILE_NAME);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] splitData = data.split(",");
                if (splitData.length == 3) {
                    String name = splitData[0].trim();
                    String surname = splitData[1].trim();
                    String phoneNumber = splitData[2].trim();
                    String fullName = name + " " + surname;
                    storage.put(fullName, phoneNumber);
                } else {
                    System.err.println("Invalid entry in database: " + data);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error handling file: " + e.getMessage());
        }
    }

    public synchronized void saveContacts() {
        try {
            File file = new File(FILE_NAME);
            FileWriter fileWriter = new FileWriter(file);
            int count = 0;
            int totalEntries = storage.size();
            for (String i : storage.keySet()) {
                String[] nameParts = i.split(" ", 2);
                if (nameParts.length == 2) {
                    String name = nameParts[0];
                    String surname = nameParts[1];
                    String phoneNumber = storage.get(i);
                    fileWriter.write(name + "," + surname + "," + phoneNumber);
                    if (count < totalEntries - 1) fileWriter.write("\n");
                    count++;
                } else {
                    System.err.println("Invalid name format in database: " + i);
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving database: " + e.getMessage());
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
        } else {
            storage.put(fullName, phoneNumber);
            saveContacts();
            return fullName + " added successfully!";
        }
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
        } else {
            return fullName + " not found!";
        }
    }

    public synchronized String listContacts() {
        if (storage.isEmpty()) return "No contacts available!";
        StringBuilder list = new StringBuilder();
        int count = 0;
        int totalEntries = storage.size();
        for (String i : storage.keySet()) {
            list.append(i).append(": ").append(storage.get(i));
            if (count < totalEntries - 1) list.append("\n");
            count++;
        }
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
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }
}
