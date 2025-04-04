Build Instructions
==================

javac AzureLabTest.java Node.java HashID.java
java AzureLabTest.java


Working Functionality
=====================

## Implemented Functionality

### 1. Node Initialization and Setup
- The node successfully initializes and sets a unique node name.
- The node correctly opens and binds to port **20110**, allowing communication over UDP.
- The node broadcasts its availability to other nodes by writing its address and port information to the network.

### 2. Network Discovery and Communication
- The node correctly attempts to establish contact with other nodes in the network.
- The **nearest node lookup process is functional**, as evidenced by outgoing queries.
- Responses to standard queries indicate that the node correctly follows protocol specifications for discovering peers.

### 3. Key-Value Storage and Retrieval
- The node successfully **initiates** key-value lookup requests.
- The node **checks its local storage for requested data** before querying other nodes.
- A **distributed data query mechanism is in place**, as indicated by external lookups when a key is not found locally.

### 4. External Network Interactions and Azure Connectivity
- The node correctly performs **DNS queries** to resolve necessary service addresses.
- Verified interactions with **Azure-based services**, including:
  - `storage.azure.net`
  - `visualstudio.com`
  - `microsoft.com`
- The system integrates with external infrastructure as expected.

### 5. Handling of Incoming Messages
- The node listens for **incoming UDP messages**.
- The system correctly parses and processes certain message types according to network protocol expectations.
