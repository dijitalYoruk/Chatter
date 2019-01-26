package com.example.chatter.Modals;

public class Group {

    public String group_id;
    public String admin_id;
    public String group_name;

    public Group() {

    }

    public Group(String group_id, String admin_id, String group_name) {
        this.group_id = group_id;
        this.admin_id = admin_id;
        this.group_name = group_name;
    }

    @Override
    public String toString() {
        return "Group{" +
                "group_id='" + group_id + '\'' +
                ", admin_id='" + admin_id + '\'' +
                ", group_name='" + group_name + '\'' +
                '}';
    }
}
