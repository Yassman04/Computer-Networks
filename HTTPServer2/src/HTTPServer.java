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

        // Read and print the request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            clientSocket.close();
            return;
        }
        System.out.println("Request Line: " + requestLine); //hi

        // Read and print headers
        StringBuilder headers = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            headers.append(line).append("\n");
        }
        System.out.println("Headers:\n" + headers.toString()); 

        // Read and print the request body (if any)
        StringBuilder body = new StringBuilder();
        while (reader.ready()) {
            body.append((char) reader.read());
        }
        if (body.length() > 0) {
            System.out.println("Body:\n" + body.toString()); 
        }

        // Respond to client
        sendResponse(writer, 200, "OK", "<html><body><h1>Received Request!</h1></body></html>");
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
