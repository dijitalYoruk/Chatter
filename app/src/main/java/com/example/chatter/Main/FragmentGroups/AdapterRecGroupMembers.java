package com.example.chatter.Main.FragmentGroups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterRecGroupMembers extends RecyclerView.Adapter<AdapterRecGroupMembers.MyViewHolder> {

    // properties
    private ArrayList<User> contacts;
    private ArrayList<User> groupMembers;
    private LayoutInflater inflater;
    private Context context;


    // constructor
    AdapterRecGroupMembers(Context context, ArrayList<User> contacts) {
        inflater = LayoutInflater.from(context);
        this.contacts = contacts;
        this.context = context;
        groupMembers = new ArrayList<>();
    }


    @NonNull
    @Override
    public AdapterRecGroupMembers.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( inflater.inflate(R.layout.list_item_group_member, viewGroup, false) );
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterRecGroupMembers.MyViewHolder myViewHolder, int i) {

        if (!contacts.get(i).image_URL.equals(""))
           Picasso.get().load(contacts.get(i).image_URL).into(myViewHolder.imgProfile);

        myViewHolder.tvUsername.setText(contacts.get(i).username);

        myViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    groupMembers.add( contacts.get( myViewHolder.getAdapterPosition() ) );
            }
        });
    }


    @Override
    public int getItemCount() {
        return contacts.size();
    }


    ArrayList<User> getGroupMembers() {
        return groupMembers;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        private ImageView imgProfile;
        private CheckBox checkBox;
        private TextView tvUsername;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}
