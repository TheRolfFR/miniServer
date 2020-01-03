package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {

    public static final String SHUTDOWN_COMMAND = "hasta_la_vista";

    MessageListener messageListener;
    AuthListener authListener;
    int personsConnected;
    private ArrayList<ClientProcessor> clientProcessors = new ArrayList<>();
    private String serverName = "server";
    private int port;
    private String ip;
    private int connectionsLimit = -1;

    public void setConnectionsLimit(int connectionsLimit) {
        if(connectionsLimit > 0)
            this.connectionsLimit = connectionsLimit;
    }

    public String getServerName() {
        return serverName;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setAuthListener(AuthListener authListener) {
        this.authListener = authListener;
    }

    @SuppressWarnings("unused")
    public Server(int port) {
        this.port = port;
        this.ip = "127.0.0.1";
    }

    public Server(int port, String serverName) {
        this.port = port;
        this.serverName = serverName;
        this.ip = "127.0.0.1";
    }

    public Server(int port, String serverName, String ip) {
        this.port = port;
        this.serverName = serverName;
        this.ip = ip;
    }

    public void run() {
        try {
            InetAddress address = InetAddress.getByName(ip);
            System.out.println("=== Starting server ===");
            ServerSocket servSocket = new ServerSocket(port, 100, address);
            System.out.println("=== Started server at " + address.getHostAddress() + ":" + port + " ===");
            System.out.println("=== Online address (if ports are opened) : " + getOnlineAddress() + ":" + port + " ===");

            //noinspection InfiniteLoopStatement
            while(true) {
                if(personsConnected < connectionsLimit) {
                    Socket newClient = servSocket.accept();
                    clientProcessors.add(ClientProcessor.addNewProcessor(newClient, this));
                }
            }

//            servSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getOnlineAddress() {
        String result;
        try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            result = sc.readLine().trim();
            if (!(result.length() > 0))
            {
                try
                {
                    InetAddress localhost = InetAddress.getLocalHost();
                    System.out.println((localhost.getHostAddress()).trim());
                    result = (localhost.getHostAddress()).trim();
                }
                catch(Exception e1)
                {
                    result = "Cannot Execute Properly";
                }
            }
        }
        catch (Exception e2)
        {
            result = "Cannot Execute Properly";
        }
        return result;
    }

    public boolean pseudoExists(String pseudoToTest) {
        // clean pseudo
        pseudoToTest = pseudoToTest.trim();

        // empty pseudos not allowed
        if(pseudoToTest.equals(""))
            return true;

        // if pseudo is terminate commande
        if(pseudoToTest.equals(SHUTDOWN_COMMAND))
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

    public int getOpenSessions() {
        return clientProcessors.size();
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

    void removeClient(ClientProcessor clientProcessor) {
        clientProcessors.remove(clientProcessor);
    }

    public interface MessageListener {
        void onMessageReceived(String pseudo, String message);
    }

    public interface AuthListener {
        void OnNewConnection(String pseudo, int personsConnected);
        void OnConnectionEnds(String pseudo, int personsConnected);
    }
}
