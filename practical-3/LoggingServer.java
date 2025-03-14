import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;

public class LoggingServer {
    private static final Logger logger = Logger.getLogger(LoggingServer.class.getName());
    private static final int PORT = 55556;
    private static List<Calculation> calculationHistory = new ArrayList<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Logging Server started on port " + PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                }
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        
        String requestLine = in.readLine();
        if (requestLine == null) return;
        
        if (requestLine.startsWith("GET")) {
            // This is a request to retrieve history
            String response = generateHistoryResponse();
            out.print(response);
            out.flush();
        } else if (requestLine.startsWith("POST")) {
            // This is a calculation to log
            StringBuilder requestBody = new StringBuilder();
            String line;
            int contentLength = 0;
            
            // Parse headers to get content length
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                }
            }
            
            // Read the request body
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                requestBody.append(buffer);
                
                // Parse the calculation data
                String[] parts = requestBody.toString().split("&");
                String expression = "";
                String result = "";
                
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        if (keyValue[0].equals("expression")) {
                            expression = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        } else if (keyValue[0].equals("result")) {
                            result = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        }
                    }
                }
                
                // Add to history if both expression and result are provided
                if (!expression.isEmpty() && !result.isEmpty()) {
                    calculationHistory.add(new Calculation(expression, result));
                    
                    // Send success response
                    out.print("HTTP/1.1 200 OK\r\n");
                    out.print("Content-Type: text/plain\r\n");
                    out.print("Content-Length: 2\r\n");
                    out.print("\r\n");
                    out.print("OK");
                    out.flush();
                } else {
                    // Send error response
                    out.print("HTTP/1.1 400 Bad Request\r\n");
                    out.print("Content-Type: text/plain\r\n");
                    out.print("Content-Length: 22\r\n");
                    out.print("\r\n");
                    out.print("Missing required fields");
                    out.flush();
                }
            }
        }
    }
    
    private static String generateHistoryResponse() {
        StringBuilder html = new StringBuilder();
        html.append("HTTP/1.1 200 OK\r\n");
        html.append("Content-Type: text/html\r\n");
        html.append("\r\n");
        html.append("<html><head><title>Calculation History</title><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #333; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append("a.button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }");
        html.append("</style></head><body>");
        html.append("<h1>Calculation History</h1>");
        
        if (calculationHistory.isEmpty()) {
            html.append("<p>No calculations have been performed yet.</p>");
        } else {
            html.append("<table>");
            html.append("<tr><th>Time</th><th>Expression</th><th>Result</th></tr>");
            
            for (Calculation calc : calculationHistory) {
                html.append("<tr>");
                html.append("<td>").append(calc.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td>");
                html.append("<td>").append(escapeHtml(calc.getExpression())).append("</td>");
                html.append("<td>").append(escapeHtml(calc.getResult())).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</table>");
        }
        
        html.append("<a href=\"http://localhost:55554\" class=\"button\">Back to Calculator</a>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}