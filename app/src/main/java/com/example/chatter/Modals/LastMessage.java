package com.example.chatter.Modals;

public class LastMessage {

    public String last_message_id;
    public boolean last_message_is_seen;


    public LastMessage(String last_message_id, boolean last_message_is_seen) {
        this.last_message_id = last_message_id;
        this.last_message_is_seen = last_message_is_seen;
    }


    public LastMessage() {
    }


    @Override
    public String toString() {
        return "LastMessage{" +
                "last_message_id='" + last_message_id + '\'' +
                ", last_message_is_seen=" + last_message_is_seen +
                '}';
    }
}