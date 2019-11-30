package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private BufferedReader br;
    private PrintWriter pw;
    private Server.MessageListener messageListener;
    private Socket socket;
    private boolean isAuthed = false;
    private String pseudo;
    private Thread thread;

    public Client(String ip, int port) throws IOException {
//        Scanner sc = new Scanner(System.in);

        socket = new Socket(ip, port);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);

        // create thread for reception
        thread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    if(br.ready()) {
                        Message m = read();
                        if(messageListener != null) {
                            messageListener.onMessageReceived(m.getPseudo(), m.getMessage());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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
        System.out.println("closing");
        if(socket != null) {
            pw.close();
            try {
                br.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
