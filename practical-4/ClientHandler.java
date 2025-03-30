import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ContactManager contactManager;

    public ClientHandler(Socket socket, ContactManager contactManager) {
        this.socket = socket;
        this.contactManager = contactManager;
    }

    @Override
    public void run() {
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(input)
            );
            OutputStream output = socket.getOutputStream()
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                sendErrorResponse(output, 400, "Bad Request");
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendErrorResponse(output, 400, "Bad Request");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];

            if ("GET".equalsIgnoreCase(method)) {
                handleGetRequest(output, path);
            } else {
                sendErrorResponse(output, 501, "Not Implemented");
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleGetRequest(OutputStream output, String path)
        throws IOException {
        Map<String, String> params = parseQueryParams(path);

        if (path.equals("/") || path.equals("/index.html")) {
            sendHomePage(output);
        } else if (path.startsWith("/addContact")) {
            handleAddContact(output, params);
        } else if (path.startsWith("/searchContact")) {
            handleSearchContact(output, params);
        } else if (path.startsWith("/deleteContact")) {
            handleDeleteContact(output, params);
        } else if (path.startsWith("/image")) {
            handleImageRequest(output, params);
        } else {
            sendErrorResponse(output, 404, "Not Found");
        }
    }

    private void sendHomePage(OutputStream output) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("<html><head><title>Contact Manager</title>");
        response.append("<style>");
        response.append(
            "body { font-family: Arial, sans-serif; margin: 20px; }"
        );
        response.append("table { border-collapse: collapse; width: 100%; }");
        response.append("th, td { border: 1px solid #ddd; padding: 8px; }");
        response.append("th { background-color: #f2f2f2; }");
        response.append("form { margin: 20px 0; }");
        response.append("</style></head><body>");
        response.append("<h1>Contact Manager</h1>");

        response.append("<h2>Add New Contact</h2>");
        response.append("<form action='/addContact' method='get'>");
        response.append("Name: <input type='text' name='name' required><br>");
        response.append(
            "Phone: <input type='text' name='number' required><br>"
        );
        response.append("<input type='submit' value='Add Contact'>");
        response.append("</form>");

        response.append("<h2>Search Contact</h2>");
        response.append("<form action='/searchContact' method='get'>");
        response.append("Name: <input type='text' name='name' required><br>");
        response.append("<input type='submit' value='Search'>");
        response.append("</form>");

        response.append("<h2>All Contacts</h2>");
        response.append(contactManager.getAllContactsHTML());

        response.append("<h2>View Contact Image</h2>");
        response.append("<form action='/image' method='get'>");
        response.append("Name: <input type='text' name='name' required><br>");
        response.append("<input type='submit' value='View Image'>");
        response.append("</form>");

        response.append("</body></html>");

        sendResponse(output, 200, "OK", response.toString());
    }

    private void handleAddContact(
        OutputStream output,
        Map<String, String> params
    ) throws IOException {
        String name = params.get("name");
        String number = params.get("number");

        if (
            name == null || number == null || name.isEmpty() || number.isEmpty()
        ) {
            sendErrorResponse(output, 400, "Name and number are required");
            return;
        }

        contactManager.addContact(name, number);
        sendRedirectResponse(output, "/");
    }

    private void handleSearchContact(
        OutputStream output,
        Map<String, String> params
    ) throws IOException {
        String name = params.get("name");

        if (name == null || name.isEmpty()) {
            sendErrorResponse(output, 400, "Name is required");
            return;
        }

        String number = contactManager.getContact(name);
        String response;

        if (number != null) {
            response =
                "<html><body><h2>Contact Found</h2>" +
                "<p><strong>Name:</strong> " +
                escapeHtml(name) +
                "</p>" +
                "<p><strong>Phone:</strong> " +
                escapeHtml(number) +
                "</p>" +
                "<p><a href='/'>Back to home</a></p>" +
                "</body></html>";
        } else {
            response =
                "<html><body><h2>Contact Not Found</h2>" +
                "<p>No contact found for: " +
                escapeHtml(name) +
                "</p>" +
                "<p><a href='/'>Back to home</a></p>" +
                "</body></html>";
        }

        sendResponse(output, 200, "OK", response);
    }

    private void handleDeleteContact(
        OutputStream output,
        Map<String, String> params
    ) throws IOException {
        String name = params.get("name");

        if (name == null || name.isEmpty()) {
            sendErrorResponse(output, 400, "Name is required");
            return;
        }

        boolean deleted = contactManager.deleteContact(name);
        sendRedirectResponse(output, "/");
    }

    private void handleImageRequest(
        OutputStream output,
        Map<String, String> params
    ) throws IOException {
        String name = params.get("name");

        if (name == null || name.isEmpty()) {
            sendErrorResponse(output, 400, "Name is required");
            return;
        }

        byte[] imageData = contactManager.getContactImage(name);
        String response;

        if (imageData != null) {
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            response =
                "<html><body><h2>Image for " +
                escapeHtml(name) +
                "</h2>" +
                "<img src='data:image/jpeg;base64," +
                base64Image +
                "' alt='Contact image' style='max-width:500px'>" +
                "<p><a href='/'>Back to home</a></p>" +
                "</body></html>";
        } else {
            response =
                "<html><body><h2>No Image Found</h2>" +
                "<p>No image available for: " +
                escapeHtml(name) +
                "</p>" +
                "<p>Here's a placeholder:</p>" +
                "<img src='https://via.placeholder.com/150' alt='Placeholder'>" +
                "<p><a href='/'>Back to home</a></p>" +
                "</body></html>";
        }

        sendResponse(output, 200, "OK", response);
    }

    private Map<String, String> parseQueryParams(String path) {
        Map<String, String> params = new HashMap<>();
        int queryStart = path.indexOf('?');
        if (queryStart == -1) return params;

        String query = path.substring(queryStart + 1);
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decodeUrl(pair.substring(0, idx));
                String value = decodeUrl(pair.substring(idx + 1));
                params.put(key, value);
            }
        }

        return params;
    }

    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    private String escapeHtml(String input) {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private void sendResponse(
        OutputStream output,
        int statusCode,
        String statusText,
        String content
    ) throws IOException {
        String response =
            "HTTP/1.1 " +
            statusCode +
            " " +
            statusText +
            "\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: " +
            content.length() +
            "\r\n" +
            "\r\n" +
            content;
        output.write(response.getBytes());
    }

    private void sendRedirectResponse(OutputStream output, String location)
        throws IOException {
        String response =
            "HTTP/1.1 302 Found\r\n" +
            "Location: " +
            location +
            "\r\n" +
            "\r\n";
        output.write(response.getBytes());
    }

    private void sendErrorResponse(
        OutputStream output,
        int statusCode,
        String message
    ) throws IOException {
        String content =
            "<html><body><h1>Error " +
            statusCode +
            ": " +
            message +
            "</h1>" +
            "<p><a href='/'>Back to home</a></p></body></html>";
        sendResponse(output, statusCode, message, content);
    }
}
