import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FTPClient {
    private Socket controlSocket;
    private BufferedReader controlReader;
    private BufferedWriter controlWriter;
    private Scanner scanner = new Scanner(System.in);

    private static final String SERVER = "127.0.0.1";
    private static final int PORT = 5050;
    private static final String USERNAME = "tester";
    private static final String PASSWORD = "password";

    private volatile boolean htmlListenerEnabled = false;
    private Thread htmlListenerThread;

    public void connect() throws IOException {
        controlSocket = new Socket(SERVER, PORT);
        controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
        controlWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
        System.out.println(readReply());
    }

    public void login() throws IOException {
        sendCommand("USER " + USERNAME);
        System.out.println(readReply());

        sendCommand("PASS " + PASSWORD);
        System.out.println(readReply());
    }

    private void sendCommand(String command) throws IOException {
        controlWriter.write(command + "\r\n");
        controlWriter.flush();
    }

    private String readReply() throws IOException {
        return controlReader.readLine();
    }

    private Socket enterPassiveMode() throws IOException {
        sendCommand("PASV");
        String response = readReply();
        System.out.println(response);

        int start = response.indexOf('(');
        int end = response.indexOf(')');
        if (start == -1 || end == -1) {
            throw new IOException("Invalid PASV response: " + response);
        }

        String[] parts = response.substring(start + 1, end).split(",");
        if (parts.length != 6) {
            throw new IOException("Invalid PASV data format.");
        }

        String ip = String.join(".", parts[0], parts[1], parts[2], parts[3]);
        int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);

        return new Socket(ip, port);
    }

    public void listFiles() throws IOException {
        Socket dataSocket = enterPassiveMode();

        sendCommand("LIST");
        System.out.println(readReply());

        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        String line;
        while ((line = dataReader.readLine()) != null) {
            System.out.println(line);
        }
        dataReader.close();
        dataSocket.close();

        System.out.println(readReply());
    }

    public void startHtmlListener() {
        if (htmlListenerEnabled) {
            System.out.println("HTML listener is already running.");
            return;
        }

        htmlListenerEnabled = true;
        htmlListenerThread = new Thread(() -> {
            try {
                File file = new File("File.html");
                if (!file.exists()) {
                    System.out.println("File.html does not exist. Listener will wait until it is created.");
                }
                long lastModified = file.exists() ? file.lastModified() : 0;

                while (htmlListenerEnabled) {
                    if (file.exists() && file.lastModified() != lastModified) {
                        lastModified = file.lastModified();
                        System.out.println("Detected change in File.html. Uploading...");

                        Socket dataSocket = enterPassiveMode();
                        sendCommand("STOR " + file.getName());
                        String response = readReply();
                        System.out.println(response);

                        if (!response.startsWith("150") && !response.startsWith("125")) {
                            System.out.println("Server rejected upload.");
                            dataSocket.close();
                            continue;
                        }

                        OutputStream dataOut = dataSocket.getOutputStream();
                        FileInputStream fileIn = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = fileIn.read(buffer)) != -1) {
                            dataOut.write(buffer, 0, bytesRead);
                        }

                        fileIn.close();
                        dataOut.close();
                        dataSocket.close();

                        System.out.println(readReply());
                        System.out.println("File.html uploaded successfully.");
                    }

                    Thread.sleep(2000); // Poll every 2 seconds
                }
            } catch (Exception e) {
                System.out.println("HTML listener error: " + e.getMessage());
            }
        });

        htmlListenerThread.start();
        System.out.println("HTML listener enabled.");
    }

    public void stopHtmlListener() {
        htmlListenerEnabled = false;
        if (htmlListenerThread != null) {
            htmlListenerThread.interrupt();
            System.out.println("HTML listener disabled.");
        }
    }

    public void disconnect() throws IOException {
        stopHtmlListener();
        sendCommand("QUIT");
        System.out.println(readReply());
        controlSocket.close();
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== FTP Client Menu ===");
            System.out.println("1. List files");
            System.out.println("2. Toggle HTML listener (" + (htmlListenerEnabled ? "ON" : "OFF") + ")");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        listFiles();
                        break;
                    case 2:
                        if (htmlListenerEnabled) {
                            stopHtmlListener();
                        } else {
                            startHtmlListener();
                        }
                        break;
                    case 3:
                        disconnect();
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        FTPClient client = new FTPClient();
        try {
            client.connect();
            client.login();
            client.showMenu();
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
