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
        String ipAddress = "Put the IP address of Azure lab machine here!";
        if (false && ipAddress.indexOf('.') == -1) {
            System.err.println("Please set your ip address!");
            System.exit(1);
        }

        try {
            Node node = new Node();
            String nodeName = "N:" + emailAddress;
            node.setNodeName(nodeName);

            int port = 20110;
            node.openPort(port);

            System.out.println("Waiting for another node to get in contact...");
            node.handleIncomingMessages(5000);

            System.out.println("Checking if other nodes are active...");
            boolean active = node.isActive("N:" + emailAddress);
            System.out.println("Other nodes active? " + active);

            System.out.println("Registering node on network...");
            node.write(nodeName, ipAddress + ":" + port);
            Thread.sleep(5000);

            System.out.println("Getting the poem...");
            for (int i = 0; i < 7; ++i) {
                String key = "D:jabberwocky" + i;
                String value = node.read(key);
                if (value == null) {
                    System.err.println("Can't find poem verse " + i);
                    System.exit(2);
                } else {
                    System.out.println(value);
                }
            }

            System.out.println("Handling incoming connections...");
            node.handleIncomingMessages(0);

        } catch (Exception e) {
            System.err.println("Exception during AzureLabTest");
            e.printStackTrace(System.err);
        }
    }
}
