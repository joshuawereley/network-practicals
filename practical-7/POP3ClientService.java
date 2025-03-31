public class POP3ClientService {
  private Socket socket;
  private BufferedReader input;
  private OutputStreamWriter output;

  public POP3ClientService(String server, int port) {
    if (!server.isEmpty()) {
      this.socket = new Socket(server, port);
      InputStreamReader isr = new InputStreamReader(socket.getInputStream());
      this.input = new BufferedReader(isr);
      this.output = new OutputStreamWriter(socket.getOutputStream());
      readServerResponse();
    }
  }

  public String readServerResponse() {
    return intput.readLine();
  }

  public boolean login(String username, String password) {
    if (!username.isEmpty() && !password.isEmpty()) {
      sendCommand("USER " + username);
      String response = readServerResponse();
      if (response.startsWith("+OK")) {
        sendCommand("PASS " + password);
        return true;
      }
      return false;
    }
  }

  public void sendCommand(String command) {
    output.write(command + "\r\n");
    output.flush();
  }

  public List<EmailMetaData> fetchEmailMetadata() {
    sendCommand("LIST");
    List<String> response = readMultilineResponse();
    Map<Integer, Integer> emailIdsAndSizes = parseListResponse(response);
    List<EmailMetaData> emails = new List<EmailMetaData>();
    for (Map.Entry<Integer, Integer> entry : emailIdsAndSizes.entrySet()) {
      int id = entry.getKey();
      int size = entry.getValue();

      sendCommand("TOP" + id + " 0");
      List<String> headers = readMultilineResponse();

      String sender = extractHeader(headers, "From:");
      String subject = extractHeader(headers, "Subject:");
      EmailMetaData emailMetaData = new EmailMetaData(id, sender, subject, size);
      emails.add(emailMetaData);
    }
    return emails;
  }

  public List<String> readMultilineResponse() {
    List<String> lines = new List<String>();
    while (String line = input.readLine() != ".") {
      lines.add(line);
    }
    return lines;
  }

  public Map<Integer, Integer> parseListResponse(List<String> listResponse) {
    Map<Integer, Integer> emailIdsAndSizes = new HashMap<Integer, Integer>();
    for (String line : listResponse) {
      if (line.equals(".") || !Character.isDigit(line.charAt(0))) {
        continue;
      }
      String[] parts = line.split(" ");
      if (parts.length >= 2) {
        int id = Integer.parseInt(parts[0]);
        int size = Integer.parseInt(parts[1]);
        emailIdsAndSizes.put(id, size);
      }
    }
    return emailIdsAndSizes;
  }
  
  public String extractHeader(List<String> headers, String headerName) {
    for (String line : headers) {
      if (line.startsWith(headerName)) {
        return line.substring(headerName.length()).trim();
      }
    }
    return "";
  }
}
