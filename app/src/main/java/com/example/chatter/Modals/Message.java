package com.example.chatter.Modals;

public class Message {

    public String message_content;
    public String message_id;
    public String message_date;
    public String message_owner;

    public Message(String message_content, String message_id, String message_date, String message_owner) {
        this.message_content = message_content;
        this.message_id = message_id;
        this.message_date = message_date;
        this.message_owner = message_owner;
    }

    public Message() {

    }

    @Override
    public String toString() {
        return "Message{" +
                "message_content='" + message_content + '\'' +
                ", message_id='" + message_id + '\'' +
                ", message_date='" + message_date + '\'' +
                ", message_owner='" + message_owner + '\'' +
                '}';
    }
}
