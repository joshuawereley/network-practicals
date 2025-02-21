public class CommandParser {

    private FriendDatabase database = new FriendDatabase();

    public CommandParser(FriendDatabase database) {
        this.database = database;
    }

    public String processCommand(String command) {
        String[] splitProcess = command.split(" ");
        switch (command) {
            case "ADD":
                if (splitProcess.length == 3) {
                    database.addContact(splitProcess[1], splitProcess[2]);
                    return "Contact added successfully!";
                } else {
                    return "Usage: ADD <name> <phonenumber>";
                }
            case "SEARCH":
                if (splitProcess.length == 2) {
                    String result = database.searchContact(splitProcess[1]);
                    return result;
                } else {
                    return "Usage: SEARCH <name>";
                }
            case "DELETE":
                if (splitProcess.length == 2) {
                    database.deleteContact(splitProcess[1]);
                    return "Contact deleted successfully!";
                } else {
                    return "Usage: DELETE <name>";
                }
            case "LIST":
                database.listContacts();
                return "Listed contacts successfully!";
            case "HELP":
                return "Available commands: ADD, SEARCH, DELETE, LIST, EXIT";
            case "EXIT":
                return "Goodbye!";
            default:
                return "Unknown command. Type HELP for commands.";
        }
    }
}
