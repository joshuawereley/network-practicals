import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class CalculatorServer {

    private static final Logger logger = Logger.getLogger(CalculatorServer.class.getName());
    private static final int PORT = 55554;
    private static CalculatorState calculatorState = new CalculatorState();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port " + PORT);
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
        if (requestLine != null && requestLine.startsWith("GET")) {
            String response = processRequest(requestLine);
            out.print(response);
            out.flush();
        }
    }

    private static String processRequest(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length > 1) {
            String query = parts[1].substring(1); // Remove leading "/"
            updateCalculatorState(query);
        }
        return generateHtmlResponse();
    }

    private static void updateCalculatorState(String query) {
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
                        } else if ("C".equals(value)) {
                            calculatorState.clear();
                        }
                        break;
                }
            }
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
}

