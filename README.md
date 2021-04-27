# miniServer
Simplified Java TCP server. Can be used for multiplayer purposes in games

## Setup guide
You can copy folders and files from the ``src`` folder directory directly inside your project.
If you don't want the examples, you can delete the ``example`` subfolder.

## How to use it

First of all don't forget to import it
```java
import com.therolf.miniServer.Client;
import com.therolf.miniServer.Message;
import com.therolf.miniServer.Server;
```

## Client side
<ol>
<li>
You need to connect to your server so what you do is that you create a client:<br>

```java
String ip = "127.0.0.1"; // match with server
int port = 3000; // match with server
Client client = new Client(ip, port);
```
</li>
<li>
What you need next is a username to be recognized ans send messages to everyone :

```java
String login = "John Wick";
client.send(login);
```
</li>
<li>Then when you are authed, the <code>isAuthed()</code> method will return true.</li>
<li>You can then finally send strings with the <code>send(String s)</code> method.</li>
<li>Alternatively, you can listen to incoming messages with the <code>isReady</code> and <code>read</code> messages:

```java
// receive messages
while(client.isRunning()) {
    if(client.isReady()) {
        Message m = client.read();
        System.out.println(m.getPseudo() + ": " + m.getMessage());
    }
}
```
</li>
</ol>

(Don't forget to wrap it with try catch for error handling)<br>
Go checkout [ClientExample.java](./src/example/ClientExample.java) for a real example.

## Server side

The server side is also simple!
<ol>
<li>
You create a server object:

```java
String port = 3000; // match with client
String serverName = "ServerExample";
String ip = "127.0.0.1"; // match with server
Server miniServer = new Server(port);
Server miniServer = new Server(port, serverName);
Server miniServer = new Server(port, serverName, ip);
```
</li>
<li>
You listen for messages and respond to them if you want:

```java
miniServer.setMessageListener((fromPseudo, message) -> {
    switch (message) {
        case "hello":
            miniServer.sendToEveryoneElse(fromPseudo, fromPseudo + " says hello");
            break;
        case "list":
            miniServer.sendFromTo(miniServer.getServerName(), fromPseudo, miniServer.getAllPseudos());
            break;
        case "ping":
            miniServer.sendFromTo(miniServer.getServerName(), fromPseudo, "pong");
            break;
        default:
            miniServer.sendToEveryone(fromPseudo, message);
            break;
    }
});
```
</li>
<li>
You run it and it does its life on its own:

```java
miniServer.run();
```
</li>

Go checkout [ServerExample.java](./src/example/ServerExample.java) for a real example.
