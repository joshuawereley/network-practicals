public class DisplayTime {
    public static void main(String args[]) {

    }

    private static int readTimezone() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/var/www/data/timezone.txt")) {
            return Integer.parseInt(reader.readLine().trim());
        }
    }
}
