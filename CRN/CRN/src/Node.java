// IN2011 Computer Networks
// Coursework 2024/2025
//
// Submission by
//  Yaseen Mneimneih
//  230008838
//  Yaseen.Mneimneih@city.ac.uk


// DO NOT EDIT starts
// This gives the interface that your code must implement.
// These descriptions are intended to help you understand how the interface
// will be used. See the RFC for how the protocol works.

import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

interface NodeInterface {

    /* These methods configure your node.
     * They must both be called once after the node has been created but
     * before it is used. */
    
    // Set the name of the node.
    public void setNodeName(String nodeName) throws Exception;

    // Open a UDP port for sending and receiving messages.
    public void openPort(int portNumber) throws Exception;


    /*
     * These methods query and change how the network is used.
     */

    // Handle all incoming messages.
    // If you wait for more than delay miliseconds and
    // there are no new incoming messages return.
    // If delay is zero then wait for an unlimited amount of time.
    public void handleIncomingMessages(int delay) throws Exception;
    
    // Determines if a node can be contacted and is responding correctly.
    // Handles any messages that have arrived.
    public boolean isActive(String nodeName) throws Exception;

    // You need to keep a stack of nodes that are used to relay messages.
    // The base of the stack is the first node to be used as a relay.
    // The first node must relay to the second node and so on.
    
    // Adds a node name to a stack of nodes used to relay all future messages.
    public void pushRelay(String nodeName) throws Exception;

    // Pops the top entry from the stack of nodes used for relaying.
    // No effect if the stack is empty
    public void popRelay() throws Exception;
    

    /*
     * These methods provide access to the basic functionality of
     * CRN-25 network.
     */

    // Checks if there is an entry in the network with the given key.
    // Handles any messages that have arrived.
    public boolean exists(String key) throws Exception;
    
    // Reads the entry stored in the network for key.
    // If there is a value, return it.
    // If there isn't a value, return null.
    // Handles any messages that have arrived.
    public String read(String key) throws Exception;

    // Sets key to be value.
    // Returns true if it worked, false if it didn't.
    // Handles any messages that have arrived.
    public boolean write(String key, String value) throws Exception;

    // If key is set to currentValue change it to newValue.
    // Returns true if it worked, false if it didn't.
    // Handles any messages that have arrived.
    public boolean CAS(String key, String currentValue, String newValue) throws Exception;

}
// DO NOT EDIT ends

// Complete this!
public class Node implements NodeInterface {

   private String nodeName;
    private int port;
    private DatagramSocket socket;
    private Map<String, String> keyValueStore = new HashMap<>();
    private Stack<String> relayStack = new Stack<>();
    private Map<String, String> addressTable = new HashMap<>();
    private Set<String> receivedMessages = new HashSet<>();

    public void setNodeName(String nodeName) throws Exception {
        this.nodeName = nodeName;
        System.out.println("Node name set to: " + nodeName);
    }

    public void openPort(int portNumber) throws Exception {
        this.port = portNumber;
        this.socket = new DatagramSocket(portNumber);
        System.out.println("Opened port: " + portNumber);
    }

    public void handleIncomingMessages(int delay) throws Exception {
        socket.setSoTimeout(delay);
        try {
            String message = receiveMessage();
            if (message != null) {
                processMessage(message);
            }
        } catch (SocketTimeoutException ignored) {
        }
    }

    public boolean isActive(String nodeName) throws Exception {
        String address = addressTable.get(nodeName);
        if (address == null) return false;
        sendMessage("G", address);
        String response = receiveMessage();
        return response != null && response.startsWith("H");
    }

    public void pushRelay(String nodeName) throws Exception {
        relayStack.push(nodeName);
    }

    public void popRelay() throws Exception {
        if (!relayStack.isEmpty()) {
            relayStack.pop();
        }
    }

    public boolean exists(String key) throws Exception {
        return keyValueStore.containsKey(key);
    }

    public String read(String key) throws Exception {
        System.out.println("Reading key: " + key);
        
        if (keyValueStore.containsKey(key)) {
            System.out.println("Found locally: " + keyValueStore.get(key));
            return keyValueStore.get(key);
        }

        System.out.println("Key not found locally. Asking other nodes...");
        for (String address : addressTable.values()) {
            System.out.println("Querying node at: " + address);
            sendMessage("R " + key, address);
            String response = receiveMessage();
            if (response != null && response.startsWith("S Y")) {
                System.out.println("Received response from other node: " + response.substring(4));
                return response.substring(4);
            }
        }

        System.out.println("No other nodes had the key.");
        return null;
    }

    public boolean write(String key, String value) throws Exception {
        System.out.println("Writing key: " + key + " with value: " + value);
        
        String nearestNode = getNearestNode(key);
        if (nearestNode == null || nearestNode.equals(getLocalAddress())) {
            keyValueStore.put(key, value);
            return true;
        }
        
        sendMessage("W " + key + " " + value, nearestNode);
        String response = receiveMessage();
        return response != null && response.startsWith("X A");
    }

    public boolean CAS(String key, String currentValue, String newValue) throws Exception {
        String nearestNode = getNearestNode(key);
        if (nearestNode != null && !nearestNode.equals(getLocalAddress())) {
            sendMessage("CAS " + key + " " + currentValue + " " + newValue, nearestNode);
            String response = receiveMessage();
            return response != null && response.startsWith("CAS A");
        }

        if (keyValueStore.containsKey(key) && keyValueStore.get(key).equals(currentValue)) {
            keyValueStore.put(key, newValue);
            return true;
        }
        return false;
    }

    private void processMessage(String message) throws Exception {
        if (receivedMessages.contains(message)) return;
        receivedMessages.add(message);

        String[] parts = message.split(" ", 3);
        String command = parts[0];

        if ("G".equals(command)) {
            sendMessage("H " + nodeName, getSenderAddress());
        } else if ("W".equals(command)) {
            if (parts.length < 3) return;
            keyValueStore.put(parts[1], parts[2]);
            sendMessage("X A", getSenderAddress());
        } else if ("R".equals(command)) {
            if (parts.length < 2) return;
            String value = keyValueStore.get(parts[1]);
            sendMessage(value != null ? "S Y " + value : "S N", getSenderAddress());
        }
    }

    private String getNearestNode(String key) throws Exception {
        return addressTable.isEmpty() ? null : addressTable.values().iterator().next();
    }

    private void sendMessage(String message, String address) throws Exception {
        String[] addrParts = address.split(":");
        InetAddress ip = InetAddress.getByName(addrParts[0]);
        int port = Integer.parseInt(addrParts[1]);

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        socket.send(packet);
    }

    private String receiveMessage() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    }

    private String getSenderAddress() {
        return "127.0.0.1:" + (port - 1);
    }

    private String getLocalAddress() {
        return "127.0.0.1:" + port;
    }
}
