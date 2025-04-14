public class POP3ResponseParser {

    public String extractHeaders(List<String> headers, String headerName) {
        for (String line : headers) {
            if (line.startsWith(headerName)) {
                return line.substring(headerName.length()).trim();
            }
        }
        return "";
    }
}
