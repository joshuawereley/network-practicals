import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POP3ResponseParser {

    public Map<Integer, Integer> parseListResponse(List<String> response)
        throws IOException {
        Map<Integer, Integer> idToSize = new HashMap<>();
        for (String line : response) {
            if (
                line.equals(".") ||
                line.startsWith("-ERR") ||
                !Character.isDigit(line.charAt(0))
            ) {
                continue;
            }
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                idToSize.put(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])
                );
            }
        }
        return idToSize;
    }

    public String extractHeader(List<String> headers, String headerName) {
        for (String line : headers) {
            if (line.startsWith(headerName)) {
                return line.substring(headerName.length()).trim();
            }
        }
        return "";
    }

    public boolean isSuccess(String response) {
        return response != null && response.startsWith("+OK");
    }
}
