import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHTTPServer {
  
  private static final int PORT 8080;

  public static void main(String[] args) {
    InetSocketAddress socketAddress = new InetSocketAddress(PORT);
    HttpServer server = HttpServer.create(socketAddress, 0);
    server.createContext("/", new MyHandler());
    server.setExecutor(null);
    server.start();
    System.out.println("Server is running on port " + PORT);
  }

}
