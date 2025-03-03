import java.io.*;

public class SwitchToGhanaTime {

    public static void main(String[] args) {
        try {
            // Write the timezone offset for Ghana (GMT+0)
            BufferedWriter writer = new BufferedWriter(
                new FileWriter("/opt/homebrew/var/www/timezone.txt")
            );
            writer.write("0");
            writer.close();

            // Print HTML response
            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html>");
            System.out.println("<head><title>Switch Timezone</title></head>");
            System.out.println("<body>");
            System.out.println("<p>Switched to Ghana Time</p>");
            System.out.println(
                "<a href=\"display_time.cgi\">Back to Current Time</a>"
            );
            System.out.println("</body>");
            System.out.println("</html>");
        } catch (IOException e) {
            System.out.println("Content-Type: text/html\n");
            System.out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
}
