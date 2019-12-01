package example;

import com.therolf.miniServer.Server;

public class ServerExample {
    public static final int PORT = 1777;

    public ServerExample() {
        Server miniServer = new Server(PORT, "serverExample", "192.168.1.66");
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
        miniServer.run();
    }

    public static void main(String[] args) {
        new ServerExample();
    }
}
