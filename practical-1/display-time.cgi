import java.io.BufferedReader;
import java.time.ZonedDateTime;

public class DisplayTime {
    public static void main(String args[]) {
        try {
            int difference = readTimezone();
            String country;
            if (difference == 2) {
                country = "South Africa";
            } else {
                country = "Ghana";
            }

            ZonedDateTime time = ZonedDateTime.now(ZoneOffset.ofHours(difference));
            String currentTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html>");
            System.out.println("<head>");
            System.out.println("<title>Current Time Display</title>");
            System.out.println("</head>");
            System.out.println("<body>");
            System.out.println("<h1>Current Time Display</h1>");
            System.out.println("<p>The current time in " + country + " is: " + currentTime + "</p>");
            System.out.println("<p>");
            System.out.println("<a href=\"/cgi-bin/set-sa-time.cgi\">Switch to South African Time</a> | ");
            System.out.println("<a href=\"/cgi-bin/set-ghana-time.cgi\">Switch to Ghana Time</a>");
            System.out.println("</p>");
            System.out.println("</body>");
            System.out.println("</html>");
        } catch (Exception e) {
            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html><head><title>Error</title></head>");
            System.out.println("<body><p>Error: " + e.getMessage() + "</p></body></html>");
        }
    }

    private static int readTimezone() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("/var/www/data/timezone.txt")) {
            return Integer.parseInt(reader.readLine().trim());
        }
    }
}
