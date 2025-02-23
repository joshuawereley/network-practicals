# Telnet Server with Contact Management

This project is a simple Telnet server that allows clients to manage a contact list. Clients can add, search, delete, and list contacts using specific commands. The server uses ANSI escape codes to format responses with colors, making the interaction more user-friendly.

## Features

- **ANSI Formatting**: The server uses ANSI escape codes to colorize responses for better readability.
- **Contact Management**: Clients can perform the following operations:
  - **ADD**: Add a new contact with a name, surname, and phone number.
  - **SEARCH**: Search for a contact by name and surname.
  - **DELETE**: Delete a contact by name and surname.
  - **LIST**: List all contacts.
  - **HELP**: Display available commands.
  - **EXIT**: Disconnect from the server.
- **Backup System**: The server automatically creates backups of the contact database whenever a contact is added or deleted.
- **Multi-threaded**: The server can handle multiple clients simultaneously using a thread pool.

## Code Structure

The project consists of the following classes:

1. **ANSIFormatter**: Handles ANSI escape codes for text formatting (e.g., colors, bold text).
2. **ClientHandler**: Manages client connections and processes incoming commands.
3. **CommandParser**: Parses and executes client commands.
4. **Constants**: Stores constant values like color codes.
5. **FriendDatabase**: Manages the contact database, including loading, saving, and backing up data.
6. **ResponseFormatter**: Formats server responses using the `ANSIFormatter`.
7. **TelnetServer**: The main server class that listens for client connections and delegates handling to `ClientHandler`.

## How to Run the Code

### Prerequisites

- Java Development Kit (JDK) 8 or later.
- A terminal or command prompt.

### Steps

1. **Clone the Repository** (if applicable):
   ```bash
   git clone https://github.com/joshuawereley/network-practicals.git
   cd network-practicals/practical-2
   ```

2. **Compile the Code**:
   Navigate to the directory containing the Java files and compile them:
   ```bash
   javac *.java
   ```

3. **Run the Server**:
   Start the Telnet server by running the `TelnetServer` class:
   ```bash
   java TelnetServer
   ```
   By default, the server listens on port `23`. You can change the port by modifying the `main` method in the `TelnetServer` class.

4. **Connect to the Server**:
   Use a Telnet client to connect to the server. For example, from the command line:
   ```bash
   telnet localhost 23
   ```

## Backup System

The server automatically creates backups of the `friends.txt` file whenever a contact is added or deleted. Backups are stored in the `Backup` folder with a timestamp in the filename.

## Logging

The server logs important events (e.g., client connections, errors) to the console using Java's built-in logging framework.
