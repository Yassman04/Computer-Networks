import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Minimal TCP server.
 *
 * Opens a listening socket, waits for one client, reads a single line from it,
 * replies with a greeting and closes the connection.
 *
 * Usage: java TCPServer [port]   (default: 4022)
 */
public class TCPServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4022;

        // A ServerSocket listens on a port and creates a new Socket for each incoming connection
        System.out.println("Opening the server socket on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server waiting for client...");
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 Writer writer = new OutputStreamWriter(clientSocket.getOutputStream())) {

                System.out.println("Client connected!");

                // Read one line from the client
                String message = reader.readLine();
                System.out.println("The client said: " + message);

                // Reply. Output is buffered, so nothing is sent until flush() is called.
                System.out.println("Sending a message to the client");
                writer.write("Nice to meet you\n");
                writer.flush();
            }
        }
    }
}
