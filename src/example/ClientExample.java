package example;

import com.therolf.miniServer.Client;

import java.io.IOException;
import java.util.Scanner;

public class ClientExample {
    private Client client;

    public ClientExample() {
        // start scanner
        Scanner sc = new Scanner(System.in);
        String str; // = ""

        // starting program
        System.out.println("=== Mini Client example ===");
        /*System.out.println("Please enter ip address:");
        if(sc.hasNextLine())
            str = sc.nextLine();*/
        str = "127.0.0.1";

        // trying to connect
        System.out.println("trying to connect to " + str + ":" + ServerExample.PORT);
        try {
            client = new Client(str, ServerExample.PORT);
            System.out.println("Please Login with your pseudo: ");

            // auth
            while(!client.isAuthed()) {
                System.out.println(client.isAuthed());
                while(sc.hasNextLine()) {
                    str = sc.nextLine();
                    client.send(str);
                    System.out.println(client.read().getMessage());
                }
            }

            System.out.println("=== Successfully logged in as " + str + " ===");

            // make a message listener
            client.setMessageListener((pseudo, message) -> System.out.println(pseudo + ": " + message));

            // send and receive messages
            while(!str.equals("bye") && sc.hasNextLine()) {
                str = sc.nextLine();
                client.send(str);
                System.out.println(client.read().getMessage());
            }

            // close client
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the socket
        if (client != null && !client.isClosed())
            client.close();
        System.out.println("=== bye ===");
    }

    public static void main(String[] args) {
        new ClientExample();
    }
}
