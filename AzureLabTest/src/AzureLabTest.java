// IN2011 Computer Networks
// Coursework 2024/2025
//
// This is a test program to show how Node.java can be used.
// It creates a single instance of Node.java.
// A bootstrapping stage gives the nodes the addresses of a few of the nodes on the Azure virtual lab
// Then it performs some basic tests on the network.
//
// Running this test is not enough to check that all of the features of your
// implementation work.  You will need to do your own testing as well.
//
// You will need to run this on the virtual lab computers.  If you run it on your own computer
// it will not be able to access the nodes on the virtual lab.
//
// You can use this to record the wireshark evidence of things working.
// But please be aware it does not test all of the features so you will need to modify it or
// write your own tests to show everything works.

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

class AzureLabTest {
     public static void main (String [] args) {
        String emailAddress = "Put your e-mail address here!";
        if (false && emailAddress.indexOf('@') == -1) {
            System.err.println("Please set your e-mail address!");
            System.exit(1);
        }
        String ipAddress = "Put the IP address of Azure lab machine here!  It should start with 10";
        if (false && ipAddress.indexOf('.') == -1) {
            System.err.println("Please set your ip address!");
            System.exit(1);
        }

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
