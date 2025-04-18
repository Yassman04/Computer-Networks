import java.io.;
  import java.net.;
  import java.security.MessageDigest;
  import java.security.NoSuchAlgorithmException;

  public class TCPClient {

      public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
          String IPAddressString = "10.200.51.18";
          InetAddress host = InetAddress.getByName(IPAddressString);

          int port = 8044;

          while (true) {
              System.out.println("Connecting to " + host + ":" + port);
              Socket clientSocket = new Socket(host, port);

              BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
              Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

              // Send message
              System.out.println("Sending message to the server");
              writer.write("Hello Server!\n");
              writer.flush();

              // Read full response
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
                  System.out.println("Could not extract required lines. Retrying...");
                  clientSocket.close();
                  continue;
              }

              // Extract expected hash from the hashLine
              String[] hashParts = hashLine.split(" ");
              String expectedHash = hashParts[hashParts.length - 1].trim();
              String actualHash = sha256(flagLine);

              System.out.println("Flag line: " + flagLine);
              System.out.println("Expected hash: " + expectedHash);
              System.out.println("Computed hash: " + actualHash);

              if (actualHash.equalsIgnoreCase(expectedHash)) {
                  System.out.println("Valid flag received.");
                  break;
              } else {
                  System.out.println("Hash mismatch. Retrying...");
              }

              clientSocket.close();

              try {
                  Thread.sleep(1000); // Optional delay
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
      }

      // Helper method to compute SHA-256
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
