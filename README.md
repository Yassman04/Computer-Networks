# Computer Networks in Java

Three networking programs built from the ground up on raw Java sockets, with no web frameworks or networking libraries: a TCP client and server, a small HTTP server, and a peer-to-peer node for a distributed key-value store that talks over UDP.

I built these during my Computer Networks module (2024/25) to understand what actually happens underneath HTTP requests and network protocols, and used Wireshark throughout to inspect the packets each program sends.

## What's inside

| Folder | What it is | Protocol |
|---|---|---|
| [`tcp-client-server/`](tcp-client-server) | A TCP server, and a client that verifies a message using a SHA-256 hash | TCP |
| [`http-server/`](http-server) | An HTTP/1.1 server that parses raw requests and sends valid responses | HTTP over TCP |
| [`crn-node/`](crn-node) | A node in a peer-to-peer network that stores and looks up key-value pairs | UDP |

## Requirements

- Java 17 or newer (`java -version` to check)
- No other dependencies

## 1. TCP client and server

**TCPServer** opens a socket, waits for a client, reads one line and replies with a greeting.

**TCPClient** connects to a "challenge" server, reads its reply line by line, and picks out two lines: a flag, and the SHA-256 hash the flag should have. It hashes the flag itself and compares the two. If they match, the message arrived intact. If they don't, or a line is missing, it reconnects and tries again.

```bash
cd tcp-client-server
javac *.java

# Terminal 1
java TCPServer 4022

# Terminal 2: talk to the server with netcat
echo "Hello" | nc localhost 4022
```

To run the client against a challenge server:

```bash
java TCPClient <host> <port>
```

The client expects the challenge server's message format (a flag line plus a "SHA-256 … should be …" line), so pointed at the simple `TCPServer` above it will keep retrying.

## 2. HTTP server

A minimal HTTP/1.1 server. For every connection it reads the request line, headers and body, prints them to the console, and returns a proper HTTP 200 response with an HTML page. It's a good way to see exactly what a browser sends when you visit a page.

```bash
cd http-server
javac HTTPServer.java
java HTTPServer 18080
```

Then open <http://localhost:18080> in a browser, or:

```bash
curl -v http://localhost:18080/hello
```

The console shows the raw request, for example:

```
Request Line: GET /hello HTTP/1.1
Headers:
Host: localhost:18080
User-Agent: curl/8.5.0
Accept: */*
```

## 3. CRN node: a peer-to-peer key-value store over UDP

Each node joins a network of peers and supports:

- **write(key, value)** to store data on the network
- **read(key)**, which checks local storage first and then asks other nodes
- **exists(key)**
- **CAS(key, current, new)**, compare-and-swap, which only updates a value if it hasn't changed
- **isActive(node)** to check whether another node is responding
- **pushRelay / popRelay** to manage a stack of nodes to route messages through

Every node has an ID made from the SHA-256 hash of its name (`HashID.java`). Nodes exchange short text messages over UDP (`W` write, `R` read, `S` read result, `G`/`H` liveness check, `CAS`) and ignore duplicate messages they have already seen.

```
          UDP
 Node A  ─────►  Node B          W key value   →   store it
 (port 20110)    (port 20111)    R key         →   S Y value / S N
                                 G             →   H nodeName
```

### Run it locally

`LocalTest` starts several nodes on your machine (ports 20110 upwards), each in its own thread, then writes a set of lines to the network and reads them back.

```bash
cd crn-node
javac *.java
java LocalTest 2      # number of nodes, 2–10
```

`AzureLabTest` connects a single node to the shared network of nodes on the university's Azure virtual lab, so it only works from inside that lab.

### Known limitations

This is a simplified version of the full protocol, and I'd change these things if I took it further:

- **Nearest-node lookup is simplified.** It forwards to the first known node instead of the node whose hash ID is closest to the key's hash, so data isn't spread across the network as the full protocol intends.
- **The relay stack is stored but not used** when sending messages.
- **Replies are sent to an assumed address** (`127.0.0.1` on a neighbouring port) rather than to the sender's real address taken from the incoming packet.
- **Messages use a simplified text format** rather than the full message format with transaction IDs, so it only fully works with other nodes running this same code.

## What I learned

- How TCP connections and UDP datagrams differ in practice: streams with guaranteed delivery against independent packets that can be lost.
- The structure of a raw HTTP request and response, and why headers like `Content-Length` and the blank line separator matter.
- Using hashing both to check message integrity (SHA-256) and to give nodes IDs in a distributed system.
- Debugging network code with Wireshark by watching the actual packets rather than guessing.

## Project structure

```
.
├── tcp-client-server/
│   ├── TCPClient.java
│   └── TCPServer.java
├── http-server/
│   └── HTTPServer.java
└── crn-node/
    ├── Node.java          # the peer-to-peer node
    ├── HashID.java        # SHA-256 node and key IDs
    ├── LocalTest.java     # runs several nodes on one machine
    └── AzureLabTest.java  # joins the university's lab network
```

## Author

**Yaseen Mneimneih** · [GitHub](https://github.com/Yassman04) · [LinkedIn](https://www.linkedin.com/in/yaseen-mneimneih-8a9305233/)
