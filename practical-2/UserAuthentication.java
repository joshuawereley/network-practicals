import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class UserAuthentication {

    private HashMap<String, String> users;
    private static final String USER_FILE = "users.txt";

    public UserAuthentication() {
        users = new HashMap<String, String>();
        loadUsers();
    }

    private void loadUsers() {
        try {
            FileReader fileReader = new FileReader(USER_FILE);
            Scanner reader = new Scanner(fileReader);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] splitData = data.split(",");
                if (splitData.length == 2) {
                    String userName = splitData[0].trim();
                    String password = splitData[1].trim();
                    users.put(userName, password);
                } else {
                    System.err.println("Invalid entry in database!\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage() + "\n");
        }
    }
}
