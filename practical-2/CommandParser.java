public class CommandParser {

    private FriendDatabase database;

    public CommandParser(FriendDatabase database) {
        this.database = database;
    }

    public String processCommand(String command) {
        String[] splitProcess = command.split(" ");
        if (splitProcess.length == 0) return "Invalid command!";
        switch (splitProcess[0].toUpperCase()) {
            case "ADD":
                if (splitProcess.length == 4) {
                    database.addContact(
                        splitProcess[1],
                        splitProcess[2],
                        splitProcess[3]
                    );
                    return (
                        splitProcess[1] +
                        " " +
                        splitProcess[2] +
                        " is successfully added!"
                    );
                } else {
                    return "Usage: ADD <name> <surname> <phonenumber>";
                }
            case "SEARCH":
                if (splitProcess.length == 2) {
                    String result = database.searchContact(
                        splitProcess[1],
                        splitProcess[2]
                    );
                    return result;
                } else {
                    return "Usage: SEARCH <name> <surname>";
                }
            case "DELETE":
                if (splitProcess.length == 3) {
                    database.deleteContact(splitProcess[1], splitProcess[2]);
                    return (
                        splitProcess[1] +
                        " " +
                        splitProcess[2] +
                        " is successfully deleted!"
                    );
                } else {
                    return "Usage: DELETE <name> <surname>";
                }
            case "LIST":
                database.listContacts();
                return "Listed contacts successfully!";
            case "HELP":
                String commands = "Available commands:\n";
                commands +=
                    "ADD <name> <surname> <phoneNumber>  - Add a new friend";
                commands +=
                    "DELETE <name> <surname>             - Remove a friend";
                commands +=
                    "SEARCH <name> <surname>             - Find a friend's phone number";
                commands +=
                    "LIST                                - Show all saved contacts";
                commands +=
                    "EXIT                                - Disconnect from the server";
                return commands;
            case "EXIT":
                return "Goodbye!";
            default:
                return "Unknown command. Type HELP for commands.";
        }
    }
}
