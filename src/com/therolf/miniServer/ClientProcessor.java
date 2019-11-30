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
        try {
            System.out.println("new connection");

            // start writer and reader
            pw = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!socket.isClosed()) {
                System.out.println("socket opened");
                try {
                    if(br != null && br.ready()) {
                        String input = read();

                        // if user is not authed
                        if(pseudo == null) {
                            // if pseudo exist
                            if(server.pseudoExists(input)) {
                                // notify with error
                                send(server.getServerName(), AUTH_FAILED_RESPONSE);
                            } else{
                                //change pseudo
                                pseudo = input;
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // end of connection
            System.out.println("end of connection");
            server.removeClient(ClientProcessor.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.removeClient(ClientProcessor.this);
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