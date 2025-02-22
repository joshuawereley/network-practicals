import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class FriendDatabase {

    HashMap<String, String> storage;

    public FriendDatabase() {
        storage = new HashMap<String, String>();
    }

    public void loadContacts() {
        try {
            File file = new File("friends.txt");
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] splitData = data.split(",");
                String name = splitData[0];
                String number = splitData[1];
                storage.put(name, number);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error handling file: " + e.getMessage());
        }
    }

    public void saveContacts() {
        try {
            File file = new File("friends.txt");
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
                writeToFile(file.getName());
            } else {
                System.out.println("File already exists");
                writeToFile(file.getName());
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
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

    public void deleteContact(String name) {
        if (storage.isEmpty() || !storage.containsKey(name)) {
            System.out.println("Contact not found!");
        } else {
            storage.remove(name);
            saveContacts();
            System.out.println("Contact deleted successfully!");
        }
    }

    public void listContacts() {
        if (storage.isEmpty()) {
            System.out.println("No contacts available!");
        } else {
            for (String i : storage.keySet()) {
                System.out.println(i + ", " + storage.get(i));
            }
        }
    }
}
