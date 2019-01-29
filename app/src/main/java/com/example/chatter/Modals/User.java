package com.example.chatter.Modals;

public class User {

    public String user_id;
    public String username;
    public String phoneNumber;
    public String image_URL;
    public String status;
    public boolean is_online;
    public String last_seen;


    public User(String user_id, String username, String phoneNumber, String image_URL, String status, boolean is_online, String last_seen) {
        this.user_id = user_id;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.image_URL = image_URL;
        this.status = status;
        this.is_online = is_online;
        this.last_seen = last_seen;
    }


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", image_URL='" + image_URL + '\'' +
                ", status='" + status + '\'' +
                ", is_online=" + is_online +
                ", last_seen='" + last_seen + '\'' +
                '}';
    }
}