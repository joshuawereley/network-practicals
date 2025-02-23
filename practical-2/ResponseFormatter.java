public class ResponseFormatter {

    private final ANSIFormatter formatter;

    public ResponseFormatter(ANSIFormatter formatter) {
        this.formatter = formatter;
    }

    public String formatResponse(String response, String colorCode) {
        return formatter.colourText(response, colorCode);
    }
}
