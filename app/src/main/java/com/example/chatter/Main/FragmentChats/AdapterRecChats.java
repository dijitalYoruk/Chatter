package com.example.chatter.Main.FragmentChats;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatter.Modals.Message;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class AdapterRecChats extends RecyclerView.Adapter<AdapterRecChats.MyViewHolder> {

    // properties
    private ArrayList<User> chatUsers;
    private LayoutInflater inflater;
    private Context context;
    private String currentUserId;


    // constructor
    AdapterRecChats(Context context, ArrayList<User> chatUsers) {
        currentUserId = FirebaseAuth.getInstance().getUid();
        inflater = LayoutInflater.from(context);
        this.chatUsers = chatUsers;
        this.context = context;
    }

    // methods

    @NonNull
    @Override
    public AdapterRecChats.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( inflater.inflate(R.layout.list_item_chat, viewGroup, false) );
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterRecChats.MyViewHolder myViewHolder, int i) {

        // getting contact user.
        final User contact = chatUsers.get(i);

        // loading profile image of contact.
        if (!contact.image_URL.equals(""))
            Picasso.get().load( contact.image_URL )
                    .placeholder(R.drawable.icon_circle_2)
                    .into( myViewHolder.imgProfile );

        // setting username
        myViewHolder.tvUserName.setText( contact.username );

        // setting date
        setChatDate(myViewHolder.tvDate, contact.user_id, contact.last_seen);

        // setting last message of the contact.
        setLastMessage(myViewHolder.tvLastSeen, contact.user_id);

        // setting new message seen symbol.
        setNewMessageSeenSymbol(myViewHolder.imgNewMessage, contact.user_id);

        // setting on click listener to item view
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatActivity( chatUsers.get( myViewHolder.getAdapterPosition() ).user_id );
            }
        });
    }


    private void setNewMessageSeenSymbol(final ImageView imgNewMessage, String user_id) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("LastMessages")
                .child(currentUserId)
                .child(user_id)
                .child("last_message_is_seen")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isSeen = (boolean) dataSnapshot.getValue();

                        if (isSeen)
                            imgNewMessage.setVisibility(View.INVISIBLE);
                        else
                            imgNewMessage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setChatDate(final TextView tvDate, String user_id, final String last_seen) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(user_id)
                .child("is_online")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isOnline = (boolean) dataSnapshot.getValue();

                        // setting last seen date.
                        if (isOnline)
                            tvDate.setText( "online" );
                        else {
                            String convertedDate = DateUtils.getRelativeTimeSpanString( Long.parseLong(last_seen),
                                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0).toString();

                            tvDate.setText( convertedDate );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setLastMessage(final TextView tvLastSeen, String user_id) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(currentUserId)
                .child(user_id)
                .orderByKey()
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data: dataSnapshot.getChildren()) {
                            String messageContent = data.child("message_content").getValue().toString();
                            tvLastSeen.setText( messageContent );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void goToChatActivity(String user_id) {
        Intent intent = new Intent(context, ActivityChat.class);
        intent.putExtra("contactId", user_id);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return chatUsers.size();
    }


    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    /**
     * View Holder Class: Holds the required views.
     */
    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        View itemView;
        ImageView imgProfile;
        ImageView imgNewMessage;
        TextView tvUserName;
        TextView tvLastSeen;
        TextView tvDate;

        // constructor
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgNewMessage = itemView.findViewById(R.id.imgNewMessage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastSeen = itemView.findViewById(R.id.tvLastSeen);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}