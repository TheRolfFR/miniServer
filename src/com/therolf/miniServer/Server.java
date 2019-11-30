package com.therolf.miniServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {

    MessageListener messageListener;
    private ArrayList<ClientProcessor> clientProcessors = new ArrayList<>();
    private String serverName = "server";
    private int port;

    public String getServerName() {
        return serverName;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @SuppressWarnings("unused")
    public Server(int port) {
        this.port = port;
    }

    public Server(int port, String serverName) {
        this.port = port;
        this.serverName = serverName;
    }

    public void run() {
        try {
            System.out.println("=== Starting server ===");
            ServerSocket servSocket = new ServerSocket(port, 100, InetAddress.getByName("127.0.0.1"));
            System.out.println("=== Started server on port " + port + " ===");

            //noinspection InfiniteLoopStatement
            while(true) {
                Socket newClient = servSocket.accept();
                clientProcessors.add(ClientProcessor.addNewProcessor(newClient, this));
            }

//            servSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean pseudoExists(String pseudoToTest) {
        // clean pseudo
        pseudoToTest = pseudoToTest.trim();

        // empty pseudos not allowed
        if(pseudoToTest.equals(""))
            return true;

        // pseudo must be different from server name
        if(pseudoToTest.equals(serverName))
            return true;

        // initialize found result
        boolean found = false;

        int i = 0;
        while (i < clientProcessors.size() && !found) {
            found = clientProcessors.get(i).getPseudo() != null && clientProcessors.get(i).getPseudo().equals(pseudoToTest);
            ++i;
        }

        // return result
        return found;
    }

    public String getAllPseudos() {
        ArrayList<String> pseudos = new ArrayList<>();
        for(ClientProcessor gscp : clientProcessors) {
            if(gscp.getPseudo() != null) {
                pseudos.add(gscp.getPseudo());
            }
        }
        return Arrays.toString(pseudos.toArray(new String[0]));
    }

    public void sendToEveryoneElse(String fromPseudo, String s) {
        for(ClientProcessor gscp : clientProcessors) {
            if(gscp.getPseudo() != null && !fromPseudo.equals(gscp.getPseudo()))
                gscp.sendIfAuthed(fromPseudo, s);
        }
    }

    @SuppressWarnings("unused")
    public void sendFromTo(String fromPseudo, String toPseudo, String message) {
        ClientProcessor gscp = null;

        // try to find it
        int i = 0;
        while (i < clientProcessors.size() && gscp == null) {
            if(clientProcessors.get(i).getPseudo() != null && clientProcessors.get(i).getPseudo().equals(toPseudo))
                gscp = clientProcessors.get(i);

            ++i;
        }

        // send it
        if(gscp != null)
            gscp.send(fromPseudo, message);
    }

    @SuppressWarnings("unused")
    public void sendToEveryone(String fromPseudo, String message) {
        for(ClientProcessor gscp : clientProcessors) {
            gscp.sendIfAuthed(fromPseudo, message);
        }
    }

    public interface MessageListener {
        void onMessageReceived(String pseudo, String message);
    }
}
