package example;

import com.therolf.miniServer.Server;

import java.util.Scanner;

public class ServerExample {
    public static final int PORT = 1777;

    public ServerExample() {
        String ip = null;
        System.out.println("Choose port to start with :");
        Scanner sc = new Scanner(System.in);
        if(sc.hasNextLine())
            ip = sc.nextLine();

        Server miniServer = new Server(PORT, "serverExample", ip);
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
