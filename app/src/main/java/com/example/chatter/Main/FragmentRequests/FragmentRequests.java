package com.example.chatter.Main.FragmentRequests;

import android.content.Context;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentRequests extends Fragment {

    // properties
    private View mainView;
    private RecyclerView recRequests;
    private ChildEventListener listener;
    private AdapterRecRequests adapter;
    private ArrayList<User> requests;
    private String currentUserId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_requests, container, false);
        recRequests = mainView.findViewById(R.id.recRequests);
        setRecyclerView();
        return mainView;
    }

    private void setRecyclerView() {
        // setting adapter.
        adapter = new AdapterRecRequests(getContext(), requests);
        recRequests.setAdapter(adapter);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recRequests.setLayoutManager(linearLayoutManager);
    }

    private void setChildEventListener() {
        listener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                addRequest( dataSnapshot.getValue().toString() );
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                removeRequest( dataSnapshot.getValue().toString() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        };

    }

    private void removeRequest(String id) {
        for (int i = 0; i < requests.size(); i++) {
            if ( requests.get(i).user_id.equals( id ) ) {
                requests.remove(i);
                adapter.notifyItemRemoved(i);
                adapter.notifyItemRangeChanged(i, requests.size());
                break;
            }
        }
    }

    private void addRequest(String userId) {

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // adding contact to the list.
                        User temp = dataSnapshot.getValue(User.class);
                        requests.add(temp);

                        // positioning recycler view.
                        adapter.notifyItemInserted(requests.size() - 1);
                        adapter.notifyItemRangeChanged(requests.size() - 1,
                                requests.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentUserId = FirebaseAuth.getInstance().getUid();
        requests = new ArrayList<>();
        setChildEventListener();
        addChildEventListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeChildEventListener();
    }

    public void addChildEventListener () {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("ChatRequests")
                .child(currentUserId)
                .addChildEventListener(listener);
    }


    public void removeChildEventListener () {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("ChatRequests")
                .child(currentUserId)
                .removeEventListener(listener);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}