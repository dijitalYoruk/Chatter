package com.example.chatter.Main.FragmentGroups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterRecMembers extends RecyclerView.Adapter<AdapterRecMembers.MyViewHolder> {

    // properties
    private LayoutInflater inflater;
    private ArrayList<User> members;
    private String adminId;
    private Context context;

    // constructor
    AdapterRecMembers(Context context, ArrayList<User> members, String adminId) {
        inflater = LayoutInflater.from(context);
        this.members = members;
        this.context = context;
        this.adminId = adminId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( inflater.inflate(R.layout.list_item_member, viewGroup, false) );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        if (!members.get(i).image_URL.equals(""))
           Picasso.get().load(members.get(i).image_URL).into(myViewHolder.imgProfile);



        if (members.get(i).user_id.equals( adminId )) {

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals( members.get(i).user_id ))
                myViewHolder.tvUsername.setText( "You" + "(admin)" );

            else myViewHolder.tvUsername.setText( members.get(i).username + "(admin)" );

        } else {
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals( members.get(i).user_id ))
                myViewHolder.tvUsername.setText( "You" );

            else myViewHolder.tvUsername.setText( members.get(i).username );
        }

    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void closeGroup(String group_id) {

        for (User member: members) {
            FirebaseDatabase.getInstance().getReference()
                    .child("GroupMembers")
                    .child(member.user_id)
                    .child(group_id)
                    .removeValue();
        }

        FirebaseDatabase.getInstance().getReference()
                .child("Groups")
                .child(group_id)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("GroupChatMessages")
                .child(group_id)
                .removeValue();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // properties
        private ImageView imgProfile;
        private TextView tvUsername;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}
