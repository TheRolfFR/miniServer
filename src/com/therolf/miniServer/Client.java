package com.therolf.miniServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private BufferedReader br;
    private PrintWriter pw;
    private Server.MessageListener messageListener;
    private Socket socket;
    private boolean isAuthed = false;

    public Client(String ip, int port) throws IOException {
        Scanner sc = new Scanner(System.in);

        socket = new Socket(ip, port);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);

        // create thread for reception
        new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    if(br.ready()) {
                        String input = read();
                        if(messageListener != null) {
                            messageListener.onMessageReceived("pseudo", input);
                        }
                    }
                } catch (IOException ignored) {}
            }
        }).start();
    }

    public void send(String string) {
        if(pw != null)
            pw.println(string);
    }

    public String read() throws IOException {
        if(br != null) {
            String res = br.readLine();
            if(res.startsWith(ClientProcessor.AUTH_COMPLETE_RESPONSE)) {
                isAuthed = true;
            }
        }
        throw new IOException("No br defined");
    }

    public boolean isAuthed() {
        return isAuthed;
    }

    public void setMessageListener(Server.MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void close() {
        if(socket != null) {
            pw.close();
            try {
                br.close();
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
