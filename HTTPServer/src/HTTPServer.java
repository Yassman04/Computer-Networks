import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {

    public HTTPServer() {}

    public static void main(String[] args) throws IOException {

        // Port numbers will be discussed in detail in lecture 5
        int port = 18080;

        // The server side is slightly more complex
        // First we have to create a ServerSocket
        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        // The ServerSocket listens and then creates as Socket object
        // for each incoming connection.
        System.out.println("Server waiting for client...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");

        // Like files, we use readers and writers for convenience
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

        String request = reader.readLine();
        String parts[] = request.split(" ");
        String method = parts[0];

        if (method.equals("GET")) {
            String path = parts[1];


            System.out.println("GETting the file " + path);

            do {
                String header = reader.readLine();
                if (header.equals(" ")) break;
            } while (true);
            if (path.equals("/") || path.equals(" /index.html") || path.equals("/webpage.html")) {
                String simpleHTML = "<html> \r\n<head>\r\n<title> Hello World</title>\r\n<body>\r\n<h1>HELLO WORLD";
            }
        }

        // We can read what the client has said
        String message = reader.readLine();
        System.out.println("The client said : " + message);

        // Sending a message to the client at the other end of the socket
        System.out.println("Sending a message to the client");
        writer.write("Nice to meet you\n");
        writer.flush();
        // To make better use of bandwidth, messages are not sent
        // until the flush method is used

        // Close down the connection
        clientSocket.close();
    }
}