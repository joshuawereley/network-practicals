import java.io.*;

public class SetSATime {

    public static void main(String[] args) {
        try {
            String dataFilePath = "/opt/homebrew/var/www/data/timezone.txt";
            FileWriter writer = new FileWriter(dataFilePath);
            writer.write("2,South Africa,Pretoria");
            writer.close();

            System.out.println("Content-type: text/html");
            System.out.println();

            System.out.println("<!DOCTYPE html>");
            System.out.println("<html lang=\"en\">");
            System.out.println("<head>");
            System.out.println("    <meta charset=\"UTF-8\">");
            System.out.println(
                "    <meta http-equiv=\"refresh\" content=\"0;url=/cgi-bin/DisplayTime.cgi\">"
            );
            System.out.println(
                "    <title>Switching to South African Time</title>"
            );
            System.out.println("</head>");
            System.out.println("<body>");
            System.out.println(
                "    <p>Switching to South African Time... If you are not redirected, <a href=\"/cgi-bin/DisplayTime.cgi\">click here</a>.</p>"
            );
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
}
