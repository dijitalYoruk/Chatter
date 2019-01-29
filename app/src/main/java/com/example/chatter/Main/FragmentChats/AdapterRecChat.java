package com.example.chatter.Main.FragmentChats;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatter.Modals.Message;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdapterRecChat extends RecyclerView.Adapter<AdapterRecChat.MyViewHolder> {

    // constants
    private final int CONTACT = 100;
    private final int RECEIVER = 200;

    // properties
    private ArrayList<Message> messages;
    private LayoutInflater inflater;
    private String currentUserId;


    // constructor
    AdapterRecChat(Context context, ArrayList<Message> messages) {
        currentUserId = FirebaseAuth.getInstance().getUid();
        inflater = LayoutInflater.from(context);
        this.messages = messages;
    }


    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).message_owner.equals( currentUserId ))
            return RECEIVER;

        else return CONTACT;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == RECEIVER) {
            return new MyViewHolder( inflater.inflate(R.layout.list_item_message_receiver, viewGroup, false ));
        } else {
            return new MyViewHolder( inflater.inflate(R.layout.list_item_message_contact, viewGroup, false ));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.tvMessage.setText( messages.get(i).message_content );
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        private TextView tvMessage;

        // constructor
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}