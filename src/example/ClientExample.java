package example;

import com.therolf.miniServer.Client;
import com.therolf.miniServer.Message;
import com.therolf.miniServer.Server;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Scanner;

public class ClientExample {
    private Client client;

    public ClientExample() {
        // start scanner
        Scanner sc = new Scanner(System.in);
        String str = ""; // = ""

        // starting program
        System.out.println("=== Mini Client example ===");
        /*System.out.println("Please enter ip address:");
        if(sc.hasNextLine())
            str = sc.nextLine();*/
        String ip = "127.0.0.1";
        int port = ServerExample.PORT;

        // trying to connect
        System.out.println("trying to connect to " + ip + ":" + port);
        try {
            client = new Client(ip, port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                client.send(Server.SHUTDOWN_COMMAND);
                System.out.println("The client is shut down!");
            }));

            System.out.println("Please Login with your pseudo: ");

            // auth
            while(!client.isAuthed() && sc.hasNextLine()) {
                str = sc.nextLine();
                client.send(str);
                System.out.println(client.read().getMessage());
            }
            System.out.println("=== Successfully logged in as " + str + " ===");

            // send messages
            Thread t = new Thread(() -> {
                while(client.isRunning()) {
                    if(sc.hasNextLine())
                        client.send(sc.nextLine());
                }
            });
            t.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    client.send(Server.SHUTDOWN_COMMAND);
                    t.interrupt();
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("The client is shut down!");
            }));

            // receive messages
            while(client.isRunning()) {
                if(client.isReady()) {
                    Message m = client.read();
                    System.out.println(m.getPseudo() + ": " + m.getMessage());
                }
            }

            System.out.println("finishing");
            //properly close client
            if(!client.isClosed())
                client.close();
        }
        catch (ConnectException e) {
            System.err.println("Couldn't connect to " + ip + ":" + port);
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("=== bye ===");
    }

    public static void main(String[] args) {
        new ClientExample();
    }
}
