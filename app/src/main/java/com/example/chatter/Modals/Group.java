package com.example.chatter.Modals;

public class Group {

    public String group_id;
    public String admin_id;
    public String group_name;
    public String image_URL;


    public Group() {

    }

    public Group(String group_id, String admin_id, String group_name, String image_URL) {
        this.group_id = group_id;
        this.admin_id = admin_id;
        this.group_name = group_name;
        this.image_URL = image_URL;
    }

    @Override
    public String toString() {
        return "Group{" +
                "group_id='" + group_id + '\'' +
                ", admin_id='" + admin_id + '\'' +
                ", group_name='" + group_name + '\'' +
                ", image_URL='" + image_URL + '\'' +
                '}';
    }
}
