import java.util.logging.Logger;

public class CommandParser {

    private static final Logger logger = Logger.getLogger(
        CommandParser.class.getName()
    );
    private final FriendDatabase database;
    private final ResponseFormatter formatter;

    public CommandParser(FriendDatabase database) {
        this.database = database;
        this.formatter = new ResponseFormatter(new ANSIFormatter());
    }

    public String processCommand(String command) {
        String[] splitProcess = command.split(" ");
        if (splitProcess.length == 0) {
            return "Invalid command!";
        }

        String commandType = splitProcess[0].toUpperCase();
        switch (commandType) {
            case "ADD":
                if (
                    validateArguments(
                        splitProcess,
                        4,
                        "ADD <name> <surname> <phonenumber>"
                    )
                ) {
                    return formatter.formatResponse(
                        database.addContact(
                            splitProcess[1],
                            splitProcess[2],
                            splitProcess[3]
                        ),
                        Constants.COLOR_SUCCESS
                    );
                } else {
                    return formatter.formatResponse(
                        "Usage: ADD <name> <surname> <phonenumber>",
                        Constants.COLOR_ERROR
                    );
                }
            case "SEARCH":
                if (
                    validateArguments(
                        splitProcess,
                        3,
                        "SEARCH <name> <surname>"
                    )
                ) {
                    return formatter.formatResponse(
                        database.searchContact(
                            splitProcess[1],
                            splitProcess[2]
                        ),
                        Constants.COLOR_INFO
                    );
                } else {
                    return formatter.formatResponse(
                        "Usage: SEARCH <name> <surname>",
                        Constants.COLOR_ERROR
                    );
                }
            case "DELETE":
                if (
                    validateArguments(
                        splitProcess,
                        3,
                        "DELETE <name> <surname>"
                    )
                ) {
                    return formatter.formatResponse(
                        database.deleteContact(
                            splitProcess[1],
                            splitProcess[2]
                        ),
                        Constants.COLOR_WARNING
                    );
                } else {
                    return formatter.formatResponse(
                        "Usage: DELETE <name> <surname>",
                        Constants.COLOR_ERROR
                    );
                }
            case "LIST":
                return formatter.formatResponse(
                    database.listContacts(),
                    Constants.COLOR_WARNING
                );
            case "HELP":
                return formatter.formatResponse(
                    getHelpMessage(),
                    Constants.COLOR_INFO
                );
            case "EXIT":
                return formatter.formatResponse(
                    "Goodbye!",
                    Constants.COLOR_ERROR
                );
            default:
                return formatter.formatResponse(
                    "Unknown command. Type HELP for commands.",
                    Constants.COLOR_ERROR
                );
        }
    }

    private boolean validateArguments(
        String[] args,
        int expectedLength,
        String usage
    ) {
        if (args.length != expectedLength) {
            logger.warning("Invalid arguments: " + String.join(" ", args));
            return false;
        }
        return true;
    }

    private String getHelpMessage() {
        return (
            "Available commands:\n" +
            "ADD <name> <surname> <phoneNumber>  - Add a new friend\n" +
            "DELETE <name> <surname>             - Remove a friend\n" +
            "SEARCH <name> <surname>             - Find a friend's phone number\n" +
            "LIST                                - Show all saved contacts\n" +
            "EXIT                                - Disconnect from the server"
        );
    }
}
