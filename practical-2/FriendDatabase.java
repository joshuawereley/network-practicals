import java.io.File;
import java.io.FileNotFoundException;
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
}
