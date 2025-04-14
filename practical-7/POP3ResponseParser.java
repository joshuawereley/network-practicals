public class POP3ResponseParser {

    public Map<Integer, Integer> parseListResponse(List<String> listResponse)
        throws IOException {
        Map<Integer, Integer> sizeMap = new HashMap<>();
        for (String line : listResponse) {
            if (
                line.equals(".") ||
                !Character.isDigit(line.charAt(0)) ||
                line.startsWith("-ERR")
            ) {
                continue;
            }
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    int size = Integer.parseInt(parts[1]);
                    sizeMap.put(id, size);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid response format", e);
                }
            }
        }
        return sizeMap;
    }

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
