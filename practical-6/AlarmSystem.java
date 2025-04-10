import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AlarmSystem {
    private static final String SMTP_SERVER = "localhost"; // Change to your SMTP server
    private static final int SMTP_PORT = 25;
    private static final String FROM_EMAIL = "alarm@yourhome.com";
    private static final String TO_EMAIL = "you@example.com";

    public static void main(String[] args) {
        System.out.println("Home Alarm System - SMTP Notifier");
        System.out.println("Press keys to simulate sensors:");
        System.out.println("m - Motion detected");
        System.out.println("d - Door opened");
        System.out.println("w - Window opened");
        System.out.println("q - Quit program");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Waiting for sensor trigger... ");
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("q")) {
                System.out.println("Shutting down alarm system...");
                break;
            }

            String subject = "";
            String body = "";

            switch (input) {
                case "m":
                    subject = "ALERT: Motion Detected!";
                    body = "Motion sensor triggered in the living room at " + getCurrentTime();
                    break;
                case "d":
                    subject = "ALERT: Door Opened!";
                    body = "Front door sensor triggered at " + getCurrentTime();
                    break;
                case "w":
                    subject = "ALERT: Window Opened!";
                    body = "Window sensor triggered in the bedroom at " + getCurrentTime();
                    break;
                default:
                    System.out.println("Invalid input. Try again.");
                    continue;
            }

            System.out.println("Sending alert: " + subject);
            sendEmail(subject, body);
        }

        scanner.close();
    }

    private static String getCurrentTime() {
        return java.time.LocalDateTime.now().toString();
    }

    private static void sendEmail(String subject, String body) {
        try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Read server greeting
            String response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("220")) {
                throw new IOException("SMTP server error");
            }

            // Send HELO command
            out.println("HELO " + InetAddress.getLocalHost().getHostName());
            response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("250")) {
                throw new IOException("HELO failed");
            }

            // Send MAIL FROM
            out.println("MAIL FROM: <" + FROM_EMAIL + ">");
            response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("250")) {
                throw new IOException("MAIL FROM failed");
            }

            // Send RCPT TO
            out.println("RCPT TO: <" + TO_EMAIL + ">");
            response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("250")) {
                throw new IOException("RCPT TO failed");
            }

            // Send DATA command
            out.println("DATA");
            response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("354")) {
                throw new IOException("DATA failed");
            }

            // Send email headers and body
            out.println("From: " + FROM_EMAIL);
            out.println("To: " + TO_EMAIL);
            out.println("Subject: " + subject);
            out.println(); // Empty line to separate headers from body
            out.println(body);
            out.println("."); // End of message
            response = in.readLine();
            System.out.println("Server: " + response);
            if (!response.startsWith("250")) {
                throw new IOException("Message sending failed");
            }

            // Send QUIT command
            out.println("QUIT");
            response = in.readLine();
            System.out.println("Server: " + response);

            System.out.println("Email sent successfully!");

        } catch (IOException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}