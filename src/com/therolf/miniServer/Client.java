package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private BufferedReader br;
    private PrintWriter pw;
    private Server.MessageListener messageListener;
    private Socket socket;
    private boolean isAuthed = false;
    private String pseudo;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    public void send(String string) {
        if(pw != null) {
            if(pseudo != null)
                pw.println(Message.toString(pseudo, string));
            else
                pw.println(string);
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

    public void setMessageListener(Server.MessageListener messageListener) {
        this.messageListener = messageListener;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!(Thread.currentThread().isInterrupted() || socket.isClosed())) {
                if (br.ready()) {
                    Message m = Client.this.read();
                    if (messageListener != null) {
                        messageListener.onMessageReceived(m.getPseudo(), m.getMessage());
                    }
                }
            }

            System.out.println("closing thread and socket");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
