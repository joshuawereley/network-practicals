public class CommandParser {

    private FriendDatabase database;
    private ANSIFormatter formatter;
    private UserAuthentication authenticator;

    public CommandParser(
        FriendDatabase database,
        UserAuthentication authenticator
    ) {
        this.database = database;
        formatter = new ANSIFormatter();
        this.authenticator = authenticator;
    }

    public String processCommand(String command) {
        String[] splitProcess = command.split(" ");
        if (splitProcess.length == 0) return "Invalid command!";
        switch (splitProcess[0].toUpperCase()) {
            case "REGISTER":
                if (splitProcess.length == 3) {
                    String username = splitProcess[1];
                    String password = splitProcess[2];
                    authenticator.addUser(username, password);
                    return formatter.colourText(
                        "User registered successfully!",
                        "32"
                    );
                } else {
                    return "Usage: REGISTER <username> <password>";
                }
            case "ADD":
                if (splitProcess.length == 4) {
                    return formatter.colourText(
                        database.addContact(
                            splitProcess[1],
                            splitProcess[2],
                            splitProcess[3]
                        ),
                        "32"
                    );
                } else {
                    return "Usage: ADD <name> <surname> <phonenumber>";
                }
            case "SEARCH":
                if (splitProcess.length == 3) {
                    return formatter.colourText(
                        database.searchContact(
                            splitProcess[1],
                            splitProcess[2]
                        ),
                        "34"
                    );
                } else {
                    return "Usage: SEARCH <name> <surname>";
                }
            case "DELETE":
                if (splitProcess.length == 3) {
                    return formatter.colourText(
                        database.deleteContact(
                            splitProcess[1],
                            splitProcess[2]
                        ),
                        "31"
                    );
                } else {
                    return "Usage: DELETE <name> <surname>";
                }
            case "LIST":
                return formatter.colourText(database.listContacts(), "33");
            case "HELP":
                String commands = "Available commands:\n";
                commands +=
                    "REGISTER <username> <password> - Register a new user\n";
                commands +=
                    "ADD <name> <surname> <phoneNumber>     - Add a new friend\n";
                commands +=
                    "DELETE <name> <surname>                - Remove a friend\n";
                commands +=
                    "SEARCH <name> <surname>                - Find a friend's phone number\n";
                commands +=
                    "LIST                                   - Show all saved contacts\n";
                commands +=
                    "EXIT                                   - Disconnect from the server";
                return formatter.colourText(commands, "36");
            case "EXIT":
                return "Goodbye!";
            default:
                return formatter.colourText(
                    "Unknown command. Type HELP for commands.",
                    "31"
                );
        }
    }
}
