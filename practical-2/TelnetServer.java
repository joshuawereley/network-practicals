import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TelnetServer {

    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;

    public TelnetServer(int port) {
        try {
            ss = new ServerSocket(port);
            System.out.println("Server has started");
            System.out.println("Wating for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(
                new BufferedInputStream(s.getInputStream())
            );

            String m = "";

            while (!m.equals("Over")) {
                try {
                    m = in.readUTF();
                    System.out.println(m);
                } catch (IOException i) {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            s.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        TelnetServer ts = new TelnetServer(23);
    }
}
