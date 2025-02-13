import java.io.BufferedReader;

public class DisplayTime {
    public static void main(String args[]) {
        int difference = readTimezone();
        String country;
        if (difference == 2) {
            country = "South Africa";
        } else {
            country = "Ghana";
        }
    }

    private static int readTimezone() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/var/www/data/timezone.txt")) {
            return Integer.parseInt(reader.readLine().trim());
        }
    }
}
