import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TCP client for a "flag" challenge server.
 *
 * The client connects, says hello, and reads the server's reply line by line.
 * The reply contains a flag line plus a line giving the SHA-256 hash the flag
 * should have. The client hashes the flag itself and compares the two: if they
 * match, the flag arrived intact; if not (or a line is missing), it reconnects
 * and tries again.
 *
 * Usage: java TCPClient [host] [port]   (defaults: localhost 4022)
 */
public class TCPClient {

    private static final int RETRY_DELAY_MS = 1000;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String hostName = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4022;
        InetAddress host = InetAddress.getByName(hostName);

        while (true) {
            System.out.println("Connecting to " + host + ":" + port);

            try (Socket clientSocket = new Socket(host, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 Writer writer = new OutputStreamWriter(clientSocket.getOutputStream())) {

                System.out.println("Sending message to the server");
                writer.write("Hello Server!\n");
                writer.flush();

                // Read the full response, keeping the two lines we need
                String flagLine = null;
                String hashLine = null;
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    System.out.println("Received: " + line);
                    if (line.contains("Challenge") && line.contains("DONE")) {
                        flagLine = line;
                    } else if (line.toLowerCase().contains("sha-256") && line.toLowerCase().contains("should be")) {
                        hashLine = line;
                    }
                }

                if (flagLine == null || hashLine == null) {
                    System.out.println("Could not find the flag and hash lines. Retrying...");
                } else {
                    // The expected hash is the last word of the hash line
                    String[] hashParts = hashLine.split(" ");
                    String expectedHash = hashParts[hashParts.length - 1].trim();
                    String actualHash = sha256(flagLine);

                    System.out.println("Flag line: " + flagLine);
                    System.out.println("Expected hash: " + expectedHash);
                    System.out.println("Computed hash: " + actualHash);

                    if (actualHash.equalsIgnoreCase(expectedHash)) {
                        System.out.println("Valid flag received.");
                        return;
                    }
                    System.out.println("Hash mismatch. Retrying...");
                }
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** Returns the SHA-256 hash of the input as a lowercase hex string. */
    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
