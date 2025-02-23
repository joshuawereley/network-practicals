import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class UserAuthentication {

    private HashMap<String, String> users;
    private static final String USER_FILE = "users.txt";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    public UserAuthentication() {
        users = new HashMap<String, String>();
        loadUsers();
    }

    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(DEFAULT_USER + "," + DEFAULT_PASSWORD + "\n");
                writer.close();
                users.put(DEFAULT_USER, DEFAULT_PASSWORD);
                System.out.println(
                    "Created default admin user: " + DEFAULT_USER
                );
            } catch (IOException e) {
                System.err.println(
                    "Error creating users file: " + e.getMessage()
                );
            }
        } else {
            try (
                FileReader fileReader = new FileReader(file);
                Scanner reader = new Scanner(fileReader)
            ) {
                while (reader.hasNextLine()) {
                    String data = reader.nextLine();
                    String[] splitData = data.split(",");
                    if (splitData.length == 2) {
                        String userName = splitData[0].trim();
                        String password = splitData[1].trim();
                        users.put(userName, password);
                    } else {
                        System.err.println(
                            "Invalid entry in users database: " + data
                        );
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }
    }

    public boolean authenticate(String username, String password) {
        return (
            users.containsKey(username) && users.get(username).equals(password)
        );
    }

    public void addUser(String username, String password) {
        users.put(username, password);
        saveUsers();
    }

    private void saveUsers() {
        try (FileWriter writer = new FileWriter(USER_FILE)) {
            for (String username : users.keySet()) {
                writer.write(username + "," + users.get(username) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
