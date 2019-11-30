package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private BufferedReader br;
    private PrintWriter pw;
    private Socket socket;
    private boolean isAuthed = false;
    private String pseudo;

    public Client(String ip, int port) throws IOException {

        this.socket = new Socket(ip, port);
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.pw = new PrintWriter(socket.getOutputStream(), true);
    }
    public void send(String string) {
        if(pw != null) {
            // close server properly
            if(string.equals(Server.SHUTDOWN_COMMAND)) {
                pw.println(string);
                pw.close();
            } else if(pseudo != null) {
                pw.println(Message.toString(pseudo, string));
            } else {
                pw.println(string);
            }
        }
    }

    public Message read() throws IOException {
        Message m; // = null
        if(br != null) {
            m = Message.fromString(br.readLine());
            if(m.message.startsWith(ClientProcessor.AUTH_COMPLETE_RESPONSE)) {
                isAuthed = true;
                pseudo = m.message.substring(ClientProcessor.AUTH_COMPLETE_RESPONSE.length());
            }
        } else {
            throw new IOException("No br defined");
        }

        return m;
    }

    public boolean isAuthed() {
        return isAuthed;
    }

    public void close() {
        if(pw != null)
            pw.close();
        if(br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(socket != null) {
            try {
                socket.close();
                System.out.println("closed socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isRunning() {
        return !isClosed();
    }

    public boolean isReady() throws IOException {
        return br.ready();
    }
}
