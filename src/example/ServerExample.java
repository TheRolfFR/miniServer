package example;

import com.therolf.miniServer.Server;

public class ServerExample {
    public static final int PORT = 1777;

    public ServerExample() {
        Server miniServer = new Server(PORT, "serverExample");
        miniServer.setMessageListener((fromPseudo, message) -> {
            System.out.println("new message received: " + message + fromPseudo);
            switch (message) {
                case "hello":
                    miniServer.sendToEveryoneElse(fromPseudo, fromPseudo + " says hello");
                    break;
                case "list":
                    miniServer.sendFromTo(miniServer.getServerName(), fromPseudo, miniServer.getAllPseudos());
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
