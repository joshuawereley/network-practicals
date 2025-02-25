import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DisplayTime {

    public static void main(String[] args) {
        try {
            System.out.println("Content-type: text/html");
            System.out.println();

            String[] data = readTimeZoneData();
            int offset = Integer.parseInt(data[0]);
            String country = data[1];
            String capital = data[2];

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(
                TimeZone.getTimeZone("GMT" + (offset >= 0 ? "+" : "") + offset)
            );
            String currentTime = dateFormat.format(new Date());

            System.out.println("<!DOCTYPE html>");
            System.out.println("<html lang=\"en\">");
            System.out.println("<head>");
            System.out.println("    <meta charset=\"UTF-8\">");
            System.out.println(
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            );
            System.out.println("    <title>Current Time</title>");
            System.out.println("    <style>");
            System.out.println(
                "        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }"
            );
            System.out.println("        h1 { color: #333; }");
            System.out.println(
                "        .time { font-size: 2em; font-weight: bold; margin: 20px 0; }"
            );
            System.out.println("        .links { margin-top: 30px; }");
            System.out.println(
                "        .links a { display: inline-block; margin-right: 15px; padding: 10px 15px; "
            );
            System.out.println(
                "                  background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; }"
            );
            System.out.println(
                "        .links a:hover { background-color: #45a049; }"
            );
            System.out.println("    </style>");
            System.out.println("</head>");
            System.out.println("<body>");
            System.out.println("    <h1>Time Zone Display</h1>");
            System.out.println(
                "    <p>The current time in " +
                capital +
                ", " +
                country +
                " is:</p>"
            );
            System.out.println(
                "    <div class=\"time\">" + currentTime + "</div>"
            );
            System.out.println("    <div class=\"links\">");
            System.out.println(
                "        <a href=\"/cgi-bin/SetSouthAfrica.cgi\">Switch to South African Time</a>"
            );
            System.out.println(
                "        <a href=\"/cgi-bin/SetGhana.cgi\">Switch to Ghana Time</a>"
            );
            System.out.println("    </div>");
            System.out.println("</body>");
            System.out.println("</html>");
        } catch (Exception e) {
            System.out.println("Content-type: text/html");
            System.out.println();
            System.out.println("<!DOCTYPE html>");
            System.out.println("<html><head><title>Error</title></head><body>");
            System.out.println("<h1>Error</h1>");
            System.out.println(
                "<p>An error occurred: " + e.getMessage() + "</p>"
            );
            System.out.println("</body></html>");
        }
    }

    private static String[] readTimeZoneData() throws IOException {
        String dataFilePath = "/opt/homebrew/var/www/data/timezone.txt";
        BufferedReader reader = new BufferedReader(
            new FileReader(dataFilePath)
        );
        String line = reader.readLine();
        reader.close();
        return line.split(",");
    }
}
