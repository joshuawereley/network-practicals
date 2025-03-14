import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.OutputStream;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ContactManager contactManager;

  public ClientHandler(Socket socket, ContactManager contactManager) {
    this.socket = socket;
    this.contactManager = contactManager;
  }

  @Override
  public void run() {
    try {
      InputSteam inputStream = socket.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      OutputStream outputStream = socket.getOutputStream();
      String line = bufferedReader.readLine();
      if (line == null && line.isEmpty() 
        return;

      System.out.println("Received: " + line);
      String[] tokens = line.split(" ");
      if (tokens.length < 2)
        return;

      String method = tokens[0];
      String path = tokens[1];
      HasMap<String, String> parameters = parseQueryParameters(path);
      String response = getResponse(path, parameters);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public HashMap<String, String> parseQueryParameters(String path) {
    HashMap<String, String> hashMap = new HashMap<String, String>();
    int queryIndex = path.indexOf('?');
    if (queryIndex == -1)
      return hashMap;
    String query = path.substring(queryIndex + 1);
    for (String pair : query.split("&")) {
      String[] keyValue = pair.split("=", 2);
      if (keyValue.length == 2)
        hashMap.put(keyValue[0], keyValue[1];
    }

    return hashMap;
  } 

  public String getResponse(String path, HashMap<String, String> parameters) {
    String httpResponse = "<html><body><h1>";
    String name = parameters.get("name");
    String number;
    if (path.startsWith("/addContact") {
      number = parameters.get("number");
      if (name != null && number != null) {
        contactManager.addContact(name, number);
        httpResponse += "Contact added: " + name;
      } else {
        httpResponse += "Missing parameters";
      }
    } else if (path.startsWith("/searchContact") {
      number = contactManager.getContact(name);
      if (number != null) {
        httpResponse += "Number: " + number;
      } else {
        httpResponse += "Contact not found";
      }
    } else if (path.startsWith("/deleteContact")) {
      boolean success = contactManager.deleteContact("name");
      if (success) {
        httpResponse += "Contact deleted: ";
      } else {
        httpResponse += "Contact not found";
      }
    } else if (path.equals("/index.html")) {
      httpResponse += serveFile("index.html");
    } else {
      httpResponse += "404 Not Found";
    }
    httpResponse += "</html></body></h1>";
    return httpResponse;
  }

}
