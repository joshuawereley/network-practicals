import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Base64;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ContactManager contactManager;

    public ClientHandler(Socket socket, ContactManager contactManager) {
        this.socket = socket;
        this.contactManager = contactManager;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            OutputStream out = socket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                sendErrorResponse(out, 400, "Empty request");
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendErrorResponse(out, 400, "Malformed request");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];
            Map<String, String> params = parseQueryParams(path);

            int contentLength = 0;
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(
                        line.substring("Content-Length:".length()).trim()
                    );
                }
            }

            String requestBody = "";
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                requestBody = new String(body);
            }

            handleRequest(out, method, path, params, requestBody);
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }

    private void handleRequest(
        OutputStream out,
        String method,
        String path,
        Map<String, String> params,
        String requestBody
    ) throws IOException {
        if ("GET".equalsIgnoreCase(method)) {
            handleGetRequest(out, path, params);
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePostRequest(out, path, params, requestBody);
        } else {
            sendErrorResponse(out, 501, "Method not implemented");
        }
    }

    private void handlePostRequest(
        OutputStream out,
        String path,
        Map<String, String> params,
        String requestBody
    ) throws IOException {
        Map<String, String> postParams = parseQueryParams("?" + requestBody);

        if (path.equals("/add")) {
            handleAddContact(out, postParams);
        } else if (path.equals("/update")) {
            handleUpdateContact(out, postParams);
        } else {
            sendErrorResponse(out, 404, "Not found");
        }
    }

    private void handleGetRequest(
        OutputStream out,
        String path,
        Map<String, String> params
    ) throws IOException {
        if (path.equals("/") || path.equals("/index.html")) {
            sendHomePage(out);
        } else if (path.startsWith("/add")) {
            handleAddContact(out, params);
        } else if (path.startsWith("/edit")) {
            handleEditContact(out, params);
        } else if (path.startsWith("/update")) {
            handleUpdateContact(out, params);
        } else if (path.startsWith("/search")) {
            handleSearchContact(out, params);
        } else if (path.startsWith("/delete")) {
            handleDeleteContact(out, params);
        } else if (path.startsWith("/view")) {
            handleViewContact(out, params);
        } else if (path.startsWith("/image")) {
            handleImageRequest(out, params);
        } else {
            sendErrorResponse(out, 404, "Not found");
        }
    }

    private void sendHomePage(OutputStream out) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
        response.append(
            "<!DOCTYPE html><html><head><title>Contact Manager</title>"
        );
        response.append("<style>");
        response.append(
            "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; "
        );
        response.append("       line-height: 1.6; margin: 0; padding: 20px; ");
        response.append("       background-color: #f5f7fa; color: #333; }");
        response.append(".container { max-width: 1200px; margin: 0 auto; ");
        response.append("            background: white; padding: 30px; ");
        response.append(
            "            border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
        );
        response.append(
            "h1 { color: #2c3e50; border-bottom: 2px solid #3498db; "
        );
        response.append("     padding-bottom: 10px; margin-top: 0; }");
        response.append(
            ".form-section, .search-section { background: #f8f9fa; "
        );
        response.append("            padding: 20px; border-radius: 5px; ");
        response.append("            margin-bottom: 30px; }");
        response.append("label { display: block; margin-bottom: 5px; ");
        response.append("        font-weight: bold; color: #2c3e50; }");
        response.append("input[type='text'], textarea { width: 100%; ");
        response.append("        padding: 10px; margin-bottom: 15px; ");
        response.append("        border: 1px solid #ddd; border-radius: 4px; ");
        response.append(
            "        box-sizing: border-box; font-family: inherit; }"
        );
        response.append("textarea { min-height: 100px; resize: vertical; }");
        response.append("input[type='submit'] { background-color: #3498db; ");
        response.append(
            "        color: white; border: none; padding: 10px 20px; "
        );
        response.append("        border-radius: 4px; cursor: pointer; ");
        response.append(
            "        font-size: 16px; transition: background-color 0.3s; }"
        );
        response.append(
            "input[type='submit']:hover { background-color: #2980b9; }"
        );
        response.append("table { width: 100%; border-collapse: collapse; ");
        response.append("        margin-top: 20px; }");
        response.append("th { background-color: #3498db; color: white; ");
        response.append("     padding: 12px; text-align: left; }");
        response.append("td { padding: 10px; border-bottom: 1px solid #eee; }");
        response.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        response.append("tr:hover { background-color: #e9f7fe; }");
        response.append("img { max-width: 100px; max-height: 100px; ");
        response.append("      border-radius: 4px; }");
        response.append(
            ".actions a { display: inline-block; margin-right: 10px; "
        );
        response.append("             padding: 5px 10px; border-radius: 3px; ");
        response.append("             text-decoration: none; }");
        response.append(
            ".view-btn { background-color: #3498db; color: white; }"
        );
        response.append(
            ".edit-btn { background-color: #2ecc71; color: white; }"
        );
        response.append(
            ".delete-btn { background-color: #e74c3c; color: white; }"
        );
        response.append(".back-btn { color: #3498db; text-decoration: none; }");
        response.append(".back-btn:hover { text-decoration: underline; }");
        response.append(".small { font-size: 0.8em; color: #7f8c8d; }");
        response.append(".error { color: #e74c3c; }");
        response.append("</style>");
        response.append("</head><body>");
        response.append("<div class='container'>");
        response.append("<h1>Contact Manager</h1>");

        response.append("<div class='form-section'>");
        response.append("<h2>Add New Contact</h2>");
        response.append("<form action='/add' method='post'>");
        response.append("<label for='name'>Name:</label>");
        response.append("<input type='text' id='name' name='name' required>");
        response.append("<label for='surname'>Surname:</label>");
        response.append(
            "<input type='text' id='surname' name='surname' required>"
        );
        response.append("<label for='number'>Phone Number:</label>");
        response.append(
            "<input type='text' id='number' name='number' required>"
        );
        response.append("<label for='image'>Image (Base64):</label>");
        response.append("<textarea id='image' name='image'></textarea>");
        response.append(
            "<p class='small'>Convert images at <a href='https://www.base64-image.de/' target='_blank'>base64-image.de</a></p>"
        );
        response.append("<input type='submit' value='Add Contact'>");
        response.append("</form></div>");

        response.append("<div class='search-section'>");
        response.append("<h2>Search Contacts</h2>");
        response.append("<form action='/search' method='get'>");
        response.append(
            "<label for='query'>Search by ID, Name, Surname or Phone:</label>"
        );
        response.append("<input type='text' id='query' name='query' required>");
        response.append("<input type='submit' value='Search'>");
        response.append("</form></div>");

        response.append("<h2>Contact List</h2>");
        response.append(contactManager.getAllContactsHTML());
        response.append("</div></body></html>");
        out.write(response.toString().getBytes());
    }

    private void handleAddContact(OutputStream out, Map<String, String> params)
        throws IOException {
        String name = InputValidator.sanitize(params.get("name"));
        String surname = InputValidator.sanitize(params.get("surname"));
        String number = InputValidator.sanitize(params.get("number"));
        String imageData = params.get("image");

        if (
            !InputValidator.isValidName(name) ||
            !InputValidator.isValidName(surname)
        ) {
            sendErrorResponse(
                out,
                400,
                "Invalid name or surname (2-50 letters only)"
            );
            return;
        }

        if (!InputValidator.isValidPhone(number)) {
            sendErrorResponse(out, 400, "Invalid phone number format");
            return;
        }

        byte[] imageBytes = null;
        if (imageData != null && !imageData.isEmpty()) {
            try {
                String base64Data = imageData.contains(",")
                    ? imageData.split(",")[1]
                    : imageData;
                imageBytes = Base64.getDecoder().decode(base64Data);
                if (!InputValidator.isValidImage(imageBytes)) {
                    sendErrorResponse(
                        out,
                        400,
                        "Invalid image format (JPEG/PNG only, max 2MB)"
                    );
                    return;
                }
            } catch (IllegalArgumentException e) {
                sendErrorResponse(out, 400, "Invalid base64 encoding");
                return;
            }
        }

        int newId = contactManager.addContact(
            new Contact(name, surname, number, imageBytes)
        );
        if (newId == -1) {
            sendErrorResponse(out, 500, "Failed to add contact");
            return;
        }
        sendRedirectResponse(out, "/");
    }

    private void handleEditContact(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String idStr = params.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendErrorResponse(out, 400, "Missing ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Contact contact = contactManager.getContact(id);
            if (contact == null) {
                sendErrorResponse(out, 404, "Contact not found");
                return;
            }

            StringBuilder response = new StringBuilder();
            response.append(
                "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
            );
            response.append(
                "<!DOCTYPE html><html><head><title>Edit Contact</title>"
            );
            response.append(
                "<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>"
            );
            response.append("</head><body>");
            response.append("<h1>Edit Contact</h1>");
            response.append("<form action='/update' method='post'>");
            response
                .append("<input type='hidden' name='id' value='")
                .append(id)
                .append("'>");
            response
                .append("Name: <input type='text' name='name' value='")
                .append(escapeHtml(contact.getName()))
                .append("' required><br>");
            response
                .append("Surname: <input type='text' name='surname' value='")
                .append(escapeHtml(contact.getSurname()))
                .append("' required><br>");
            response
                .append("Phone: <input type='text' name='number' value='")
                .append(escapeHtml(contact.getNumber()))
                .append("' required><br>");
            response.append(
                "Image (Base64): <textarea name='image' rows='4' cols='50'>"
            );
            if (contact.getImage() != null) {
                String base64Image = Base64.getEncoder()
                    .encodeToString(contact.getImage());
                response.append(base64Image);
            }
            response.append("</textarea><br>");
            response.append(
                "<small>Update base64 at <a href='https://www.base64-image.de/' target='_blank'>base64-image.de</a></small><br>"
            );
            response.append("<input type='submit' value='Update'>");
            response.append("</form>");
            response.append("<a href='/'>Cancel</a>");
            response.append("</body></html>");
            out.write(response.toString().getBytes());
        } catch (NumberFormatException e) {
            sendErrorResponse(out, 400, "Invalid ID");
        }
    }

    private void handleUpdateContact(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String idStr = params.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendErrorResponse(out, 400, "Missing ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = InputValidator.sanitize(params.get("name"));
            String surname = InputValidator.sanitize(params.get("surname"));
            String number = InputValidator.sanitize(params.get("number"));
            String imageData = params.get("image");

            if (
                !InputValidator.isValidName(name) ||
                !InputValidator.isValidName(surname)
            ) {
                sendErrorResponse(
                    out,
                    400,
                    "Invalid name or surname (2-50 letters only)"
                );
                return;
            }

            if (!InputValidator.isValidPhone(number)) {
                sendErrorResponse(out, 400, "Invalid phone number format");
                return;
            }

            byte[] imageBytes = null;
            if (imageData != null && !imageData.isEmpty()) {
                try {
                    String base64Data = imageData.contains(",")
                        ? imageData.split(",")[1]
                        : imageData;
                    imageBytes = Base64.getDecoder().decode(base64Data);
                    if (!InputValidator.isValidImage(imageBytes)) {
                        sendErrorResponse(
                            out,
                            400,
                            "Invalid image format (JPEG/PNG only, max 2MB)"
                        );
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    sendErrorResponse(out, 400, "Invalid base64 encoding");
                    return;
                }
            }

            if (
                contactManager.updateContact(
                    id,
                    new Contact(name, surname, number, imageBytes)
                )
            ) {
                sendRedirectResponse(out, "/");
            } else {
                sendErrorResponse(out, 404, "Contact not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(out, 400, "Invalid ID");
        }
    }

    private void handleSearchContact(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String query = params.get("query");
        if (query == null || query.isEmpty()) {
            sendErrorResponse(out, 400, "Search query required");
            return;
        }

        List<Map.Entry<Integer, Contact>> results =
            contactManager.searchContacts(query);
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
        response.append(
            "<!DOCTYPE html><html><head><title>Search Results</title>"
        );
        response.append(
            "<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>"
        );
        response.append("</head><body>");
        response.append("<h1>Search Results</h1>");

        if (results.isEmpty()) {
            response
                .append("<p>No contacts found matching '")
                .append(escapeHtml(query))
                .append("'</p>");
        } else {
            response.append(
                "<table border='1'><tr><th>ID</th><th>Name</th><th>Surname</th><th>Phone</th><th>Image</th><th>Actions</th></tr>"
            );
            for (Map.Entry<Integer, Contact> entry : results) {
                Contact c = entry.getValue();
                response
                    .append("<tr><td>")
                    .append(entry.getKey())
                    .append("</td>");
                response
                    .append("<td>")
                    .append(escapeHtml(c.getName()))
                    .append("</td>");
                response
                    .append("<td>")
                    .append(escapeHtml(c.getSurname()))
                    .append("</td>");
                response
                    .append("<td>")
                    .append(escapeHtml(c.getNumber()))
                    .append("</td>");
                response
                    .append("<td>")
                    .append(
                        c.getImage() != null
                            ? "<img src='/image?id=" +
                            entry.getKey() +
                            "' style='max-width:100px'>"
                            : "No image"
                    )
                    .append("</td>");
                response
                    .append("<td><a href='/view?id=")
                    .append(entry.getKey())
                    .append("'>View</a> ");
                response
                    .append("<a href='/edit?id=")
                    .append(entry.getKey())
                    .append("'>Edit</a> ");
                response
                    .append("<a href='/delete?id=")
                    .append(entry.getKey())
                    .append("'>Delete</a></td></tr>");
            }
            response.append("</table>");
        }
        response.append("<p><a href='/'>Back to all contacts</a></p>");
        response.append("</body></html>");
        out.write(response.toString().getBytes());
    }

    private void handleDeleteContact(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String idStr = params.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendErrorResponse(out, 400, "Missing ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (contactManager.deleteContact(id)) {
                sendRedirectResponse(out, "/");
            } else {
                sendErrorResponse(out, 404, "Contact not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(out, 400, "Invalid ID");
        }
    }

    private void handleViewContact(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String idStr = params.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendErrorResponse(out, 400, "Missing ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Contact contact = contactManager.getContact(id);
            if (contact == null) {
                sendErrorResponse(out, 404, "Contact not found");
                return;
            }

            StringBuilder response = new StringBuilder();
            response.append(
                "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n"
            );
            response.append(
                "<!DOCTYPE html><html><head><title>Contact Details</title>"
            );
            response.append(
                "<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>"
            );
            response.append("</head><body>");
            response.append("<h1>Contact Details</h1>");
            response
                .append("<p><strong>ID:</strong> ")
                .append(id)
                .append("</p>");
            response
                .append("<p><strong>Name:</strong> ")
                .append(escapeHtml(contact.getName()))
                .append("</p>");
            response
                .append("<p><strong>Surname:</strong> ")
                .append(escapeHtml(contact.getSurname()))
                .append("</p>");
            response
                .append("<p><strong>Phone:</strong> ")
                .append(escapeHtml(contact.getNumber()))
                .append("</p>");
            if (contact.getImage() != null) {
                response.append("<p><strong>Image:</strong><br>");
                response
                    .append("<img src='/image?id=")
                    .append(id)
                    .append("' style='max-width:300px'></p>");
            } else {
                response.append("<p><strong>Image:</strong> No image</p>");
            }
            response
                .append("<p><a href='/edit?id=")
                .append(id)
                .append("'>Edit</a> | ");
            response.append("<a href='/'>Back to list</a></p>");
            response.append("</body></html>");
            out.write(response.toString().getBytes());
        } catch (NumberFormatException e) {
            sendErrorResponse(out, 400, "Invalid ID");
        }
    }

    private void handleImageRequest(
        OutputStream out,
        Map<String, String> params
    ) throws IOException {
        String idStr = params.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendErrorResponse(out, 400, "Missing ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Contact contact = contactManager.getContact(id);
            if (contact == null || contact.getImage() == null) {
                sendErrorResponse(out, 404, "Image not found");
                return;
            }

            byte[] imageData = contact.getImage();
            String contentType = (imageData[0] == (byte) 0xFF)
                ? "image/jpeg"
                : "image/png";
            String response =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " +
                contentType +
                "\r\n" +
                "Content-Length: " +
                imageData.length +
                "\r\n\r\n";
            out.write(response.getBytes());
            out.write(imageData);
        } catch (NumberFormatException e) {
            sendErrorResponse(out, 400, "Invalid ID");
        }
    }

    private Map<String, String> parseQueryParams(String path) {
        Map<String, String> params = new HashMap<>();
        int queryStart = path.indexOf('?');
        if (queryStart == -1) return params;

        String query = path.substring(queryStart + 1);
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                try {
                    String key = URLDecoder.decode(
                        pair.substring(0, idx),
                        "UTF-8"
                    );
                    String value = URLDecoder.decode(
                        pair.substring(idx + 1),
                        "UTF-8"
                    );
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {}
            }
        }
        return params;
    }

    private void sendRedirectResponse(OutputStream out, String location)
        throws IOException {
        String response =
            "HTTP/1.1 302 Found\r\nLocation: " + location + "\r\n\r\n";
        out.write(response.getBytes());
    }

    private void sendErrorResponse(
        OutputStream out,
        int statusCode,
        String message
    ) throws IOException {
        String response =
            "HTTP/1.1 " +
            statusCode +
            " " +
            message +
            "\r\n" +
            "Content-Type: text/html\r\n\r\n" +
            "<!DOCTYPE html><html><head><title>Error " +
            statusCode +
            "</title></head>" +
            "<body><h1>Error " +
            statusCode +
            "</h1><p>" +
            escapeHtml(message) +
            "</p>" +
            "<p><a href='/'>Return to Home</a></p></body></html>";
        out.write(response.getBytes());
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private static class InputValidator {

        private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s'-]{2,50}$"
        );
        private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+\\d\\s()-]{7,20}$"
        );
        private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024;

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
}
