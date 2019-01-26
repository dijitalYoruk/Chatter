package com.example.chatter.Main.FragmentGroups;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatter.Modals.Group;
import com.example.chatter.R;

import java.util.ArrayList;

public class AdapterRecGroups extends RecyclerView.Adapter<AdapterRecGroups.MyViewHolder> {

    // properties
    private ArrayList<Group> groups;
    private LayoutInflater inflater;
    private Context context;

    // constructor
    AdapterRecGroups(Context context, ArrayList<Group> groups) {
        inflater = LayoutInflater.from(context);
        this.groups = groups;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( inflater.inflate(R.layout.list_item_group, viewGroup, false) );
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.tvGroupName.setText( groups.get(i).group_name );

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivityChat(myViewHolder.getAdapterPosition());
            }
        });
    }


    private void goToActivityChat(int position) {
        Intent intent = new Intent(context, ActivityGroupChat.class);
        intent.putExtra("groupId", groups.get(position).group_id);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return groups.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        TextView tvGroupName;
        View itemView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            this.itemView = itemView;
        }
    }
}
