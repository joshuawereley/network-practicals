public class SetSATime {
    public static void main(String args[]) {
        try {
            try (FileWriter writer = new FileWriter("/var/www/data/timezone.txt")) {
                writer.write("2");
            }

            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html>");
            System.out.println("<head>");
            System.out.println("<title>Time Zone Updated</title>");
            System.out.println("<meta http-equiv=\"refresh\" content=\"0;url=/cgi-bin/display-time.cgi\">");
            System.out.println("</head>");
            System.out.println("<body>");
            System.out.println("<p>Switching to South African Time...</p>");
            System.out.println("</body>");
            System.out.println("</html>");
        } catch (Exception e) {
            System.out.println("Content-Type: text/html\n");
            System.out.println("<!DOCTYPE HTML>");
            System.out.println("<html><head><title>Error</title></head>");
            System.out.println("<body><p>Error: " + e.getMessage() + "</p></body></html>");
        }
    }
}
