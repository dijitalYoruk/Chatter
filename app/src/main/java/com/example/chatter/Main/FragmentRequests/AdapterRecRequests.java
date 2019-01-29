package com.example.chatter.Main.FragmentRequests;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterRecRequests extends RecyclerView.Adapter<AdapterRecRequests.MyViewHolder> {

    // properties
    private ArrayList<User> requests;
    private LayoutInflater inflater;


    // constructor
    AdapterRecRequests(Context context, ArrayList<User> requests) {
        inflater = LayoutInflater.from(context);
        this.requests = requests;
    }


    @NonNull
    @Override
    public AdapterRecRequests.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder( inflater.inflate(R.layout.list_item_request, viewGroup, false) );
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterRecRequests.MyViewHolder myViewHolder, int i) {

        UniversalImageLoader.setImage(requests.get(i).image_URL,
                myViewHolder.imgProfile, myViewHolder.progressBar);

        myViewHolder.tvUserName.setText( requests.get(i).username );

        if (!requests.get(i).status.equals(""))
            myViewHolder.tvStatus.setText( requests.get(i).status );

        myViewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest( myViewHolder.getAdapterPosition() );
            }
        });

        myViewHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyRequest( myViewHolder.getAdapterPosition() );
            }
        });
    }


    private void denyRequest(int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String requestOwnerId = requests.get(position).user_id;

        FirebaseDatabase.getInstance().getReference()
                .child("ChatRequests")
                .child(currentUserId)
                .child(requestOwnerId)
                .removeValue();

        requests.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged( position, requests.size() );
    }


    private void acceptRequest(int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String requestOwnerId = requests.get(position).user_id;

        FirebaseDatabase.getInstance().getReference()
                .child("ChatRequests")
                .child(currentUserId)
                .child(requestOwnerId)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(currentUserId)
                .child(requestOwnerId)
                .setValue(requestOwnerId);

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(requestOwnerId)
                .child(currentUserId)
                .setValue(currentUserId);

        requests.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged( position, requests.size() );
    }


    @Override
    public int getItemCount() {
        return requests.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        // properties
        private View itemView;
        private ImageView imgProfile;
        private TextView tvUserName;
        private TextView tvStatus;
        private Button btnAccept;
        private Button btnCancel;
        private ProgressBar progressBar;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}