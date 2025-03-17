import java.io.*;
import java.net.*;

public class HTTPServer {

    public static void main(String[] args) throws IOException {
        int port = 18080; // Keep port as 18080 as required

        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) { // Keep server running to handle multiple requests
            System.out.println("Server waiting for client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            handleClient(clientSocket);
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             Writer writer = new OutputStreamWriter(clientSocket.getOutputStream())) {

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                clientSocket.close();
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendResponse(writer, 400, "Bad Request", "Invalid Request Format");
                clientSocket.close();
                return;
            }

            String method = parts[0];
            String path = parts[1];

            // Read headers until an empty line is found
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Ensures full HTTP request is read
            }

            if (!method.equals("GET")) {
                sendResponse(writer, 400, "Bad Request", "Only GET method is supported.");
            } else if (path.equals("/") || path.equals("/index.html")) {
                sendResponse(writer, 200, "OK", "<html><body><h1>Welcome to the HTTP Server!</h1></body></html>");
            } else {
                sendResponse(writer, 404, "Not Found", "<html><body><h1>404 - Page Not Found</h1></body></html>");
            }

            clientSocket.close();
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
