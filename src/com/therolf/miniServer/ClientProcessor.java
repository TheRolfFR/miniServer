package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientProcessor implements Runnable {

    static final String AUTH_COMPLETE_RESPONSE = "Welcome ";
    private static final String AUTH_FAILED_RESPONSE = "Auth failed. Please choose another pseudo.";
    private static final String MESSAGE_SEPARATOR = " | ";

    private Socket socket;
    private PrintWriter pw;
    private BufferedReader br;
    private String pseudo;

    public String getPseudo() {
        return pseudo;
    }

    private ClientProcessor(Socket socket, Server server) {
        this.socket = socket;

        new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    if(br.ready()) {
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
                            }
                        } else {
                            // the user is authed
                            // decode its message

                            int separatorIndex = input.indexOf(MESSAGE_SEPARATOR);
                            String pseudo = "unknown";
                            String message = input;
                            if(separatorIndex > -1) {
                                pseudo = input.substring(0, separatorIndex);
                                message = input.substring(separatorIndex);
                            }

                            if(server.messageListener != null) {
                                server.messageListener.onMessageReceived(pseudo, message);
                            }
                        }
                    }
                } catch (IOException ignored) {}
            }
        }).start();
    }

    static ClientProcessor addNewProcessor(Socket socket, Server server) {
	    ClientProcessor c = new ClientProcessor(socket, server);
        new Thread(c).start();
        return c;
    }

    public void send(String fromPseudo, String message) {
        if(pw != null)
            pw.println(fromPseudo + MESSAGE_SEPARATOR + message);
    }

    @Override
    public void run() {
        try {
            System.out.println("new connection");

            // start writer and reader
            pw = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendIfAuthed(String fromPseudo, String string) {
        if(this.pseudo != null)
            send(fromPseudo, string);
    }

    private String read() throws IOException {
        if(br != null) {
            return br.readLine();
        }
        throw new IOException("No br defined");
    }
}
