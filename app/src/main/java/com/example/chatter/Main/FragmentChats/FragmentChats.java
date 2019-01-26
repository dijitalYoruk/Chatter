package com.example.chatter.Main.FragmentChats;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatter.Main.FragmentAllUsers.FragmentAllUsers;
import com.example.chatter.Main.MainActivity;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentChats extends Fragment {

    // properties
    private View mainView;
    private AdapterRecChats adapter;
    private RecyclerView recChats;
    private ArrayList<User> chats;
    private String currentUserId;
    private ChildEventListener listener;

    // methods

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_chats, container, false);
        recChats = mainView.findViewById(R.id.recChats);
        setRecyclerView();
        setOnClickListeners();
        return mainView;
    }


    private void setRecyclerView() {
        // setting adapter.
        adapter = new AdapterRecChats(getContext(), chats);
        recChats.setAdapter(adapter);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recChats.setLayoutManager(linearLayoutManager);
    }


    private void setOnClickListeners() {
        FloatingActionButton fabViewUsers = mainView.findViewById(R.id.fabViewUsers);

        fabViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).goToContactsFragment();
            }
        });
    }


    private void setChildEventListener() {
        listener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                addChat( dataSnapshot.getValue().toString() );
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                removeChat( dataSnapshot.getValue().toString() );
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


    private void addChat(String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // adding contact to the list.
                        User temp = dataSnapshot.getValue(User.class);
                        chats.add(temp);

                        // positioning recycler view.
                        adapter.notifyItemInserted(chats.size() - 1);
                        adapter.notifyItemRangeChanged(chats.size() - 1, chats.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void removeChat(String id) {
        for (int i = 0; i < chats.size(); i++) {
            if ( chats.get(i).user_id.equals( id ) ) {
                chats.remove(i);
                adapter.notifyItemRemoved(i);
                adapter.notifyItemRangeChanged(i, chats.size());
                break;
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentUserId = FirebaseAuth.getInstance().getUid();
        chats = new ArrayList<>();
        setChildEventListener();
        addChildEventListener();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        removeChildEventListener();
    }


    private void addChildEventListener() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Contacts")
                .child(currentUserId)
                .addChildEventListener(listener);
    }


    private void removeChildEventListener() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Contacts")
                .child(currentUserId)
                .removeEventListener(listener);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}