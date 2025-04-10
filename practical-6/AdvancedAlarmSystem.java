import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class AdvancedAlarmSystem {
    // Configuration
    private static final String SMTP_SERVER = "localhost";
    private static final int SMTP_PORT = 25;
    private static final String FROM_EMAIL = "alarm@yourdomain.com";
    private static final List<String> TO_EMAILS = Arrays.asList(
            "admin@yourdomain.com",
            "mobile@yourdomain.com");
    private static final String DOMAIN = "yourdomain.com";

    // Sensor mappings with priorities
    private static final Map<String, Sensor> SENSORS = Map.of(
            "m", new Sensor("Motion", "Living Room", 1),
            "d", new Sensor("Door", "Front Entrance", 1),
            "w", new Sensor("Window", "Bedroom", 2),
            "f", new Sensor("Fire", "Kitchen", 3),
            "s", new Sensor("Security", "Safe Room", 3));

    // Email tracking
    private static final ConcurrentHashMap<String, EmailRecord> sentEmails = new ConcurrentHashMap<>();
    private static final String LOG_FILE = "alarm_log.txt";

    public static void main(String[] args) {
        System.out.println("=== Advanced Home Alarm System ===");
        System.out.println("Sensor Keys:");
        SENSORS.forEach((key, sensor) -> System.out.printf(
                "%s - %s (%s) Priority %d\n",
                key, sensor.type, sensor.location, sensor.priority));
        System.out.println("l - View sent alerts");
        System.out.println("q - Quit program\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Waiting for sensor trigger... ");
                String input = scanner.nextLine().toLowerCase();

                if (input.equals("q")) {
                    System.out.println("Shutting down alarm system...");
                    break;
                }

                if (input.equals("l")) {
                    displaySentAlerts();
                    continue;
                }

                Sensor sensor = SENSORS.get(input);
                if (sensor == null) {
                    System.out.println("Invalid input. Try again.");
                    continue;
                }

                String messageId = sendAlertEmail(sensor);
                logAlert(messageId, sensor);
            }
        }
    }

    private static String sendAlertEmail(Sensor sensor) {
        String messageId = "<" + UUID.randomUUID() + "@" + DOMAIN + ">";
        String subject = "ALERT: " + sensor.type + " Detected in " + sensor.location;
        String priority = sensor.priority == 3 ? "1 (Highest)" : sensor.priority == 2 ? "2 (High)" : "3 (Normal)";

        String body = String.join("\r\n",
                "ALARM TRIGGERED",
                "Type: " + sensor.type,
                "Location: " + sensor.location,
                "Time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME),
                "Priority: " + sensor.priority,
                "",
                "This is an automated alert from your home security system.");

        try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // SMTP Protocol with enhanced error handling
            expectResponse(in, 220, "Connection failed");

            out.println("EHLO " + InetAddress.getLocalHost().getHostName());
            readMultiLineResponse(in, 250, "EHLO failed");

            out.println("MAIL FROM: <" + FROM_EMAIL + ">");
            expectResponse(in, 250, "MAIL FROM failed");

            for (String to : TO_EMAILS) {
                out.println("RCPT TO: <" + to + ">");
                expectResponse(in, 250, "RCPT TO failed for " + to);
            }

            out.println("DATA");
            expectResponse(in, 354, "DATA failed");

            // RFC 5322 Compliant Headers
            out.println("From: " + FROM_EMAIL);
            out.println("To: " + String.join(", ", TO_EMAILS));
            out.println("Subject: " + subject);
            out.println("Message-ID: " + messageId);
            out.println("Date: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            out.println("X-Priority: " + priority);
            out.println("Disposition-Notification-To: " + FROM_EMAIL);
            out.println("MIME-Version: 1.0");
            out.println("Content-Type: text/plain; charset=utf-8");
            out.println();

            // RFC-compliant line folding
            body = body.replaceAll("(.{78})", "$1\r\n ");
            out.println(body);

            out.println(".");
            expectResponse(in, 250, "Message sending failed");

            out.println("QUIT");
            expectResponse(in, 221, "QUIT failed");

            System.out.println("Alert sent successfully! Message ID: " + messageId);
            return messageId;

        } catch (IOException e) {
            System.err.println("Error sending alert: " + e.getMessage());
            return null;
        }
    }

    // New method to handle multi-line responses
    private static void readMultiLineResponse(BufferedReader in, int expectedCode, String errorMsg) throws IOException {
        String response;
        do {
            response = in.readLine();
            System.out.println("SMTP: " + response);
            if (response == null || response.startsWith(expectedCode + " ")) {
                break;
            }
        } while (response.startsWith(expectedCode + "-"));

        if (response == null || !response.startsWith(String.valueOf(expectedCode))) {
            throw new IOException(errorMsg + ". Response: " + response);
        }
    }
    private static void expectResponse(BufferedReader in, int expectedCode, String errorMsg) throws IOException {
        String response = in.readLine();
        System.out.println("SMTP: " + response);
        if (response == null || !response.startsWith(String.valueOf(expectedCode))) {
            throw new IOException(errorMsg + ". Response: " + response);
        }
    }

    private static void logAlert(String messageId, Sensor sensor) {
        if (messageId == null)
            return;

        EmailRecord record = new EmailRecord(
                messageId,
                ZonedDateTime.now(),
                sensor.type,
                sensor.location,
                sensor.priority);

        sentEmails.put(messageId, record);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(record.toLogEntry());
        } catch (IOException e) {
            System.err.println("Warning: Could not write to log file: " + e.getMessage());
        }
    }

    private static void displaySentAlerts() {
        System.out.println("\n=== Sent Alerts ===");
        if (sentEmails.isEmpty()) {
            System.out.println("No alerts sent yet.");
            return;
        }

        // Read from log file for complete history
        try (Scanner fileScanner = new Scanner(new File(LOG_FILE))) {
            while (fileScanner.hasNextLine()) {
                System.out.println(fileScanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Log file not found.");
        }
        System.out.println();
    }

    // Helper classes
    static class Sensor {
        String type;
        String location;
        int priority;

        Sensor(String type, String location, int priority) {
            this.type = type;
            this.location = location;
            this.priority = priority;
        }
    }

    static class EmailRecord {
        String messageId;
        ZonedDateTime timestamp;
        String alertType;
        String location;
        int priority;

        EmailRecord(String messageId, ZonedDateTime timestamp, String alertType, String location, int priority) {
            this.messageId = messageId;
            this.timestamp = timestamp;
            this.alertType = alertType;
            this.location = location;
            this.priority = priority;
        }

        String toLogEntry() {
            return String.format("[%s] %s at %s (Priority %d) - Message ID: %s",
                    timestamp.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    alertType,
                    location,
                    priority,
                    messageId);
        }
    }
}