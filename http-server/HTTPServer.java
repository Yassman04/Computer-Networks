import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A small HTTP/1.1 server written directly on top of TCP sockets.
 *
 * For every connection it parses the request line, headers and (optional) body,
 * logs them to the console, and answers with a valid HTTP 200 response
 * containing a short HTML page. Point a browser or curl at it to see the raw
 * request your client sends.
 *
 * Usage: java HTTPServer [port]   (default: 18080)
 */
public class HTTPServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 18080;

        System.out.println("Opening the server socket on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Server waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");
                handleClient(clientSocket);
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             Writer writer = new OutputStreamWriter(clientSocket.getOutputStream())) {

            // Request line, e.g. "GET /index.html HTTP/1.1"
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }
            System.out.println("Request Line: " + requestLine);

            // Headers end at the first blank line
            StringBuilder headers = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append("\n");
            }
            System.out.println("Headers:\n" + headers);

            // Body (if any) is whatever is left to read
            StringBuilder body = new StringBuilder();
            while (reader.ready()) {
                body.append((char) reader.read());
            }
            if (body.length() > 0) {
                System.out.println("Body:\n" + body);
            }

            sendResponse(writer, 200, "OK", "<html><body><h1>Received Request!</h1></body></html>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(Writer writer, int statusCode, String statusText, String body) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("Content-Length: " + body.length() + "\r\n");
        writer.write("Connection: close\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }
}
