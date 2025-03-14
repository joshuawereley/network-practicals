import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

public class CalculatorServer {

    private static final Logger logger = Logger.getLogger(CalculatorServer.class.getName());
    private static final int PORT = 55554;
    private static final String LOGGING_SERVER = "localhost";
    private static final int LOGGING_PORT = 55556;
    private static CalculatorState calculatorState = new CalculatorState();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Calculator Server started on port " + PORT);
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
        if (requestLine == null) {
            sendErrorResponse(out, 400, "Bad Request");
            return;
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            sendErrorResponse(out, 400, "Bad Request");
            return;
        }

        String method = requestParts[0];
        String path = requestParts[1];
        
        // Parse headers
        Map<String, String> headers = parseHeaders(in);
        
        // Handle HTTP methods
        if (method.equals("GET")) {
            processGetRequest(out, path);
        } else {
            sendErrorResponse(out, 405, "Method Not Allowed");
        }
    }

    private static Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colonPos = line.indexOf(':');
            if (colonPos > 0) {
                String headerName = line.substring(0, colonPos).trim();
                String headerValue = line.substring(colonPos + 1).trim();
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    private static void processGetRequest(PrintWriter out, String path) {
        if (path.startsWith("/?")) {
            String query = path.substring(2);
            boolean evaluated = updateCalculatorState(query);
            
            // If we evaluated an expression, log it
            if (evaluated) {
                logCalculation(calculatorState.getExpression(), calculatorState.getResult());
            }
        }
        
        out.print(generateHtmlResponse());
        out.flush();
    }

    private static boolean updateCalculatorState(String query) {
        boolean evaluated = false;
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                switch (key) {
                    case "digit":
                        calculatorState.addDigit(value);
                        break;
                    case "operator":
                        calculatorState.addOperator(value);
                        break;
                    case "action":
                        if ("=".equals(value)) {
                            calculatorState.evaluate();
                            evaluated = true;
                        } else if ("C".equals(value)) {
                            calculatorState.clear();
                        }
                        break;
                }
            }
        }
        return evaluated;
    }

    private static void logCalculation(String expression, String result) {
        try (Socket socket = new Socket(LOGGING_SERVER, LOGGING_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String requestBody = "expression=" + URLEncoder.encode(expression, StandardCharsets.UTF_8) + 
                               "&result=" + URLEncoder.encode(result, StandardCharsets.UTF_8);
            
            // Send HTTP POST request to logging server
            out.println("POST / HTTP/1.1");
            out.println("Host: " + LOGGING_SERVER);
            out.println("Content-Type: application/x-www-form-urlencoded");
            out.println("Content-Length: " + requestBody.length());
            out.println();
            out.print(requestBody);
            out.flush();
            
            // Read response (but we don't need to do anything with it)
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (in.readLine() != null && !in.readLine().isEmpty()) {
                // Skip headers
            }
            
        } catch (IOException e) {
            logger.warning("Failed to log calculation: " + e.getMessage());
        }
    }

    private static String generateHtmlResponse() {
        StringBuilder html = new StringBuilder();
        html.append("HTTP/1.1 200 OK\r\n");
        html.append("Content-Type: text/html\r\n");
        html.append("\r\n");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f0f0; }");
        html.append(".calculator { border: 1px solid #ccc; padding: 20px; border-radius: 10px; background-color: #fff; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
        html.append(".calculator input[type='text'] { width: 100%; padding: 10px; margin-bottom: 10px; font-size: 18px; border: 1px solid #ccc; border-radius: 5px; }");
        html.append(".calculator button { width: 23%; padding: 10px; margin: 1%; font-size: 18px; border: 1px solid #ccc; border-radius: 5px; background-color: #f9f9f9; cursor: pointer; }");
        html.append(".calculator button:hover { background-color: #e9e9e9; }");
        html.append(".history-button { display: block; width: 100%; margin-top: 15px; padding: 10px; background-color: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; text-align: center; text-decoration: none; font-size: 16px; }");
        html.append(".history-button:hover { background-color: #45a049; }");
        html.append("</style></head><body>");
        html.append("<div class=\"calculator\">");
        html.append("<h1>Calculator</h1>");
        html.append("<form action=\"/\" method=\"GET\">");
        html.append("<input type=\"text\" name=\"expression\" value=\"")
            .append(escapeHtml(calculatorState.getExpression()))
            .append("\" readonly><br>");
        html.append("<input type=\"text\" name=\"result\" value=\"")
            .append(escapeHtml(calculatorState.getResult()))
            .append("\" readonly><br>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"1\">1</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"2\">2</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"3\">3</button>");
        html.append("<button type=\"submit\" name=\"operator\" value=\"+\">+</button><br>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"4\">4</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"5\">5</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"6\">6</button>");
        html.append("<button type=\"submit\" name=\"operator\" value=\"-\">-</button><br>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"7\">7</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"8\">8</button>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"9\">9</button>");
        html.append("<button type=\"submit\" name=\"operator\" value=\"*\">*</button><br>");
        html.append("<button type=\"submit\" name=\"digit\" value=\"0\">0</button>");
        html.append("<button type=\"submit\" name=\"operator\" value=\"/\">/</button>");
        html.append("<button type=\"submit\" name=\"action\" value=\"=\">=</button>");
        html.append("<button type=\"submit\" name=\"action\" value=\"C\">C</button>");
        html.append("</form>");
        html.append("<a href=\"http://localhost:55556\" class=\"history-button\"> History</a>");
        html.append("</div>");
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

    private static void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        out.print("HTTP/1.1 " + statusCode + " " + message + "\r\n");
        out.print("Content-Type: text/html\r\n");
        out.print("\r\n");
        out.print("<html><body><h1>" + statusCode + " " + message + "</h1></body></html>");
        out.flush();
    }
}

