import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class FriendDatabase {

    private HashMap<String, String> storage;
    private static final String FILE_NAME = "friends.txt";

    public FriendDatabase() {
        storage = new HashMap<String, String>();
        loadContacts();
    }

    public void loadContacts() {
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

    public void saveContacts() {
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
                    String phoneNumber = nameParts[2];
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

    public void writeToFile(String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName);
            for (String i : storage.keySet()) {
                //TODO: NEED TO FORMAT BETTER WHEN WRITING TO FILE
                writer.write(i + "," + storage.get(i));
            }
            writer.close();
            System.out.println("Successfully wrote to " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public String addContact(String name, String surname, String phoneNumber) {
        String fullName = name + " " + surname;
        if (!storage.containsKey(fullName)) {
            return "Contact already exists!";
        } else {
            storage.put(fullName, phoneNumber);
            saveContacts();
            return "Contact added successfully!";
        }
    }

    public String searchContact(String name, String surname) {
        String fullName = name + " " + surname;
        return storage.getOrDefault(fullName, "Contact not found!");
    }

    public String deleteContact(String name, String surname) {
        String fullName = name + " " + surname;
        if (storage.remove(fullName) != null) {
            saveContacts();
            return "Contact deleted successfully!";
        } else {
            return "Contact not found!";
        }
    }

    public String listContacts() {
        if (storage.isEmpty()) return "No contacts available!";
        StringBuilder list = new StringBuilder();
        for (String i : storage.keySet()) {
            list.append(i).append(": ").append(storage.get(i)).append("\n");
        }
        return list.toString();
    }
}
