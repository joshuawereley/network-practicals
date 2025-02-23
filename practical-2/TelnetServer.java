import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TelnetServer {

    private static final Logger logger = Logger.getLogger(
        TelnetServer.class.getName()
    );
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService executorService =
        Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public TelnetServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server has started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected: " + socket.getInetAddress());
                executorService.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            logger.severe("Error: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        new TelnetServer(23);
    }
}
