package com.example.chatter.Main.FragmentAllUsers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentAllUsers extends Fragment {

    // properties
    private RecyclerView rcvContacts;
    private ArrayList<User> contacts;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView =  inflater.inflate(R.layout.fragment_all_users, container, false);
        rcvContacts = mainView.findViewById(R.id.rcvUsers);
        contacts = new ArrayList<>();
        getContactsData();
        return mainView;
    }


    private void getContactsData() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot data: dataSnapshot.getChildren()) {
                            User temp = data.getValue(User.class);

                            if (temp.user_id.equals( currentUserId ))
                                continue;

                            contacts.add(temp);
                        }

                        setRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setRecyclerView() {
        // setting adapter.
        AdapterRecAllUsers adapterRecGroups = new AdapterRecAllUsers(getContext(), contacts);
        rcvContacts.setAdapter(adapterRecGroups);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvContacts.setLayoutManager(linearLayoutManager);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}