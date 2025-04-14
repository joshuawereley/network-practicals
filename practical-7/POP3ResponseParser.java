public class POP3ResponseParser {

    public String extractHeaders(List<String> headers, String headerName) {
        for (String line : headers) {
            if (line.startsWith(headerName)) {
                return line.substring(headerName.length()).trim();
            }
        }
        return "";
    }

    public Map<Integer, String> parseUIDLResponse(List<String> uidlResponse) {
        Map<Integer, String> idToUIDL = new HashMap<>();
        for (String line : uidlResponse) {
            if (line.equals(".") || !Character.isDigit(line.charAt(0))) {
                continue;
            }
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                idToUIDL.put(Integer.parseInt(parts[0]), parts[1]);
            }
        }
        return idToUIDL;
    }
}
