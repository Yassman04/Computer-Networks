// Test program for running a single Node against the module's shared network
// of nodes hosted on an Azure virtual lab.
// It creates a single instance of Node.java.
// A bootstrapping stage gives the nodes the addresses of a few of the nodes on the Azure virtual lab
// Then it performs some basic tests on the network.
//
// It only works from inside the virtual lab; from any other machine the lab
// nodes are unreachable. Use LocalTest to try the node on your own computer.
//
// Run it while capturing with Wireshark to see the UDP traffic between nodes.

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

class AzureLabTest {
    public static void main(String[] args) {
        try {
            // Create the Node and initialize
            Node node = new Node();
            String nodeName = "N:Node";
            node.setNodeName(nodeName);

            int port = 20110; // Node port
            node.openPort(port);

            System.out.println("Waiting for another node to get in contact...");
            node.handleIncomingMessages(5000); // Wait for 5 seconds for incoming messages

            // Check if any other nodes are active
            System.out.println("Checking if other nodes are active...");
            boolean active = node.isActive("N:Node");
            System.out.println("Other nodes active? " + active);

            // Register the node on the network (write address of node to network)
            System.out.println("Registering node on network...");
            node.write(nodeName, "127.0.0.1:" + port);  // Using localhost for testing
            Thread.sleep(5000); // Wait for some time to ensure registration

            // Test reading keys (poem) from the network
            System.out.println("Getting the poem...");
            for (int i = 0; i < 7; ++i) {
                String key = "D:jabberwocky" + i;
                String value = node.read(key);
                if (value == null) {
                    System.err.println("Can't find poem verse " + i);
                    System.exit(2); // Exit if any key cannot be found
                } else {
                    System.out.println(value); // Print each poem verse
                }
            }

            // Handling incoming messages indefinitely (simulating continuous operation)
            System.out.println("Handling incoming connections...");
            node.handleIncomingMessages(0); // Listen indefinitely for incoming messages

        } catch (Exception e) {
            System.err.println("Exception during AzureLabTest");
            e.printStackTrace(System.err);
        }
    }
}
