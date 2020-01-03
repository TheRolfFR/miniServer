package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientProcessor implements Runnable {

    static final String AUTH_COMPLETE_RESPONSE = "Welcome ";
    private static final String AUTH_FAILED_RESPONSE = "Auth failed. Please choose another pseudo.";

    private Socket socket;
    private Server server;
    private PrintWriter pw;
    private BufferedReader br;
    private String pseudo;

    public String getPseudo() {
        return pseudo;
    }

    private ClientProcessor(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("new connection");
        try {

            // start writer and reader
            pw = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!socket.isClosed()) {
//                System.out.println(socket.isClosed() + " + " + socket.isConnected() + " + " + socket.isInputShutdown() + socket.isOutputShutdown());
                if(br != null && br.ready()) {
                    String input = read();

                    if(input.equals(Server.SHUTDOWN_COMMAND)) {
                        if(pseudo != null && server.authListener != null) {
                            --server.personsConnected;
                            server.authListener.OnConnectionEnds(pseudo , server.personsConnected);
                        }
                        socket.close();
                    } else if(pseudo == null) {
                        // if pseudo exist
                        if(server.pseudoExists(input)) {
                            // notify with error
                            send(server.getServerName(), AUTH_FAILED_RESPONSE);
                        } else{
                            //change pseudo
                            pseudo = input;

                            ++server.personsConnected;
                            if(server.authListener != null) {
                                server.authListener.OnNewConnection(pseudo, server.personsConnected);
                            }

                            send(server.getServerName(), AUTH_COMPLETE_RESPONSE + pseudo);
                            System.out.println("connection successful with " + pseudo);
                        }
                    } else {
                        // the user is authed
                        // decode its message
                        Message m = Message.fromString(input);
                        if(server.messageListener != null) {
                            server.messageListener.onMessageReceived(m.getPseudo(), m.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("end of connection");
        System.out.println((pseudo != null) ? " with " + pseudo : "");
        server.removeClient(ClientProcessor.this);
        System.out.println(server.getOpenSessions() + " open sessions left");
    }

    static ClientProcessor addNewProcessor(Socket socket, Server server) {
	    ClientProcessor c = new ClientProcessor(socket, server);
        new Thread(c).start();
        return c;
    }

    public void send(String fromPseudo, String message) {
        if(pw != null)
            pw.println(Message.toString(fromPseudo, message));
    }

    public void sendIfAuthed(String fromPseudo, String string) {
        if(this.pseudo != null)
            this.send(fromPseudo, string);
    }

    private String read() throws IOException {
        if(br != null) {
            return br.readLine();
        }
        throw new IOException("No br defined");
    }
}
