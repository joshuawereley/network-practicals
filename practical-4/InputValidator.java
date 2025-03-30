import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]{2,50}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+\\d\\s()-]{7,20}$"
    );
    private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) return true;
        if (imageData.length > MAX_IMAGE_SIZE) return false;

        boolean isJPEG =
            (imageData[0] & 0xFF) == 0xFF && (imageData[1] & 0xFF) == 0xD8;
        boolean isPNG =
            (imageData[0] & 0xFF) == 0x89 && (imageData[1] & 0xFF) == 0x50;
        return isJPEG || isPNG;
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim();
    }
}
