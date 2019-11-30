package com.therolf.miniServer;

public class Message {
    static final String MESSAGE_SEPARATOR = " | ";
    String pseudo, message;

    @SuppressWarnings("unused")
    public String getPseudo() {
        return pseudo;
    }

    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }

    public Message(String pseudo, String message) {
        this.pseudo = pseudo;
        this.message = message;
    }

    public static Message fromString(String inputString) {

        int separatorIndex = inputString.indexOf(Message.MESSAGE_SEPARATOR);
        String pseudo = "unknown";
        String message = inputString;
        if(separatorIndex > -1) {
            pseudo = inputString.substring(0, separatorIndex);
            message = inputString.substring(separatorIndex + MESSAGE_SEPARATOR.length());
        }

        return new Message(pseudo, message);
    }

    static String toString(String pseudo, String message) {
        return pseudo + Message.MESSAGE_SEPARATOR + message;
    }
}
