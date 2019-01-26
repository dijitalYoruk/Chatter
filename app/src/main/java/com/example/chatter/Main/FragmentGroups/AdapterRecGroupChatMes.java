package com.example.chatter.Main.FragmentGroups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatter.Modals.Message;
import com.example.chatter.Modals.User;
import com.example.chatter.R;

import java.util.ArrayList;

public class AdapterRecGroupChatMes extends RecyclerView.Adapter<AdapterRecGroupChatMes.MyViewHolder> {

    // constants
    private final int CONTACT = 100;
    private final int RECEIVER = 200;

    // properties
    private ArrayList<Message> messages;
    private LayoutInflater inflater;
    private User currentUser;
    private Context context;


    // constructor
    AdapterRecGroupChatMes(Context context, ArrayList<Message> messages, User currentUser) {
        inflater = LayoutInflater.from(context);
        this.messages = messages;
        this.context = context;
        this.currentUser = currentUser;
    }


    @Override
    public int getItemViewType(int position) {
        if ( messages.get(position).message_owner.equals( currentUser.username ) )
            return RECEIVER;

        return CONTACT;
    }


    @NonNull
    @Override
    public AdapterRecGroupChatMes.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == RECEIVER)
            return new MyViewHolder( inflater.inflate(R.layout.list_item_message_group_receiver, viewGroup, false) );

        return new MyViewHolder( inflater.inflate(R.layout.list_item_message_group_contact, viewGroup, false) );
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterRecGroupChatMes.MyViewHolder myViewHolder, int i) {
        myViewHolder.tvMessageContent.setText( messages.get(i).message_content );
        myViewHolder.tvUsername.setText( messages.get(i).message_owner );

        // date
        Long messageDate = Long.parseLong( messages.get(i).message_date );

        String convertedDate = DateUtils.getRelativeTimeSpanString( messageDate,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0).toString();

        if (convertedDate.equals("0 minutes ago"))
            myViewHolder.tvDate.setText( "now" );

        else myViewHolder.tvDate.setText( convertedDate );
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        private View itemView;
        private TextView tvMessageContent;
        private TextView tvUsername;
        private TextView tvDate;

        // constructor
        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
