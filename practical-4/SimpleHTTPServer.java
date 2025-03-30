import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleHTTPServer {

    private static final int PORT = 8081;
    private static final String UPLOAD_DIR = "uploads";
    private static final String SAMPLE_IMAGES_DIR = "sample_images";

    public static void main(String[] args) {
        // Create necessary directories if they don't exist
        createDirectoryIfNotExists(UPLOAD_DIR);
        createDirectoryIfNotExists(SAMPLE_IMAGES_DIR);

        ContactManager contactManager = new ContactManager();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on http://localhost:" + PORT);
            System.out.println(
                "Upload directory: " + Paths.get(UPLOAD_DIR).toAbsolutePath()
            );
            System.out.println(
                "Sample images directory: " +
                Paths.get(SAMPLE_IMAGES_DIR).toAbsolutePath()
            );
            System.out.println("Press Ctrl+C to stop the server");

            // Add sample images to sample_images directory
            createSampleImages();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println(
                        "New connection from: " + clientSocket.getInetAddress()
                    );

                    // Handle each client connection in a new thread
                    new Thread(
                        new ClientHandler(clientSocket, contactManager)
                    ).start();
                } catch (IOException e) {
                    System.err.println(
                        "Error accepting client connection: " + e.getMessage()
                    );
                }
            }
        } catch (IOException e) {
            System.err.println(
                "Could not start server on port " + PORT + ": " + e.getMessage()
            );
            System.exit(1);
        }
    }

    private static void createDirectoryIfNotExists(String directory) {
        try {
            if (!Files.exists(Paths.get(directory))) {
                Files.createDirectories(Paths.get(directory));
                System.out.println(
                    "Created directory: " +
                    Paths.get(directory).toAbsolutePath()
                );
            }
        } catch (IOException e) {
            System.err.println(
                "Failed to create directory '" +
                directory +
                "': " +
                e.getMessage()
            );
        }
    }

    private static void createSampleImages() {
        try {
            // Create sample john.jpg
            if (!Files.exists(Paths.get(SAMPLE_IMAGES_DIR, "john.jpg"))) {
                byte[] johnImage = generateSampleImage("John Doe");
                Files.write(
                    Paths.get(SAMPLE_IMAGES_DIR, "john.jpg"),
                    johnImage
                );
            }

            // Create sample alice.png
            if (!Files.exists(Paths.get(SAMPLE_IMAGES_DIR, "alice.png"))) {
                byte[] aliceImage = generateSampleImage("Alice Johnson");
                Files.write(
                    Paths.get(SAMPLE_IMAGES_DIR, "alice.png"),
                    aliceImage
                );
            }
        } catch (IOException e) {
            System.err.println(
                "Error creating sample images: " + e.getMessage()
            );
        }
    }

    private static byte[] generateSampleImage(String text) {
        // Generate a simple image with the contact's name
        String svg =
            "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>" +
            "<rect width='200' height='200' fill='#3498db'/>" +
            "<text x='100' y='100' font-family='Arial' font-size='16' " +
            "fill='white' text-anchor='middle' dominant-baseline='middle'>" +
            text +
            "</text></svg>";
        return svg.getBytes();
    }
}
