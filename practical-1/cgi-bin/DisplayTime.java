import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DisplayTime {

    public static void main(String[] args) {
        try {
            // Read timezone offset from file
            BufferedReader reader = new BufferedReader(
                new FileReader("/opt/homebrew/var/www/timezone.txt")
            );
            int offset = Integer.parseInt(reader.readLine().trim());
            reader.close();

            // Create zone with proper offset
            ZoneId zone = ZoneId.ofOffset(
                "GMT",
                java.time.ZoneOffset.ofHours(offset)
            );

            // Get current time in that zone
            ZonedDateTime now = ZonedDateTime.now(zone);
            String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Print HTML response
            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html>");
            System.out.println("<head><title>Current Time</title></head>");
            System.out.println("<body>");

            // Display the current time and which timezone it is
            String timezone = (offset == 0) ? "Ghana" : "South African";
            System.out.println(
                "<p>The current time in " +
                timezone +
                " Time Zone is " +
                time +
                "</p>"
            );

            // Links to switch timezones
            System.out.println(
                "<a href=\"switch_to_sa_time.cgi\">Switch to South African Time</a><br>"
            );
            System.out.println(
                "<a href=\"switch_to_ghana_time.cgi\">Switch to Ghana Time</a>"
            );

            System.out.println("</body>");
            System.out.println("</html>");
        } catch (IOException e) {
            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html>");
            System.out.println("<head><title>Error</title></head>");
            System.out.println("<body>");
            System.out.println("<p>Error: " + e.getMessage() + "</p>");
            System.out.println("</body>");
            System.out.println("</html>");
        }
    }
}
