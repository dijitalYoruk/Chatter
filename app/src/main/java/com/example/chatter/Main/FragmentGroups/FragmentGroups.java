package com.example.chatter.Main.FragmentGroups;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatter.Modals.Group;
import com.example.chatter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentGroups extends Fragment {

    // properties
    private RecyclerView rcvGroups;
    private ArrayList<Group> groups;
    private AdapterRecGroups adapterRecGroups;
    private ChildEventListener listener;
    private String currentUserId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_groups, container, false);
        currentUserId = FirebaseAuth.getInstance().getUid();
        rcvGroups = mainView.findViewById(R.id.rcvGroups);
        groups = new ArrayList<>();
        setRecyclerView();
        setChildEventListener();
        addChildEventListener();
        return mainView;
    }


    private void setRecyclerView() {
        // setting adapter.
        adapterRecGroups = new AdapterRecGroups(getContext(), groups);
        rcvGroups.setAdapter(adapterRecGroups);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvGroups.setLayoutManager(linearLayoutManager);
    }


    private void setChildEventListener() {
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                addGroup( dataSnapshot.getValue().toString() );
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                removeGroup( dataSnapshot.getValue().toString() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        };
    }


    private void addChildEventListener() {
        FirebaseDatabase.getInstance().getReference()
                .child("GroupMembers")
                .child(currentUserId)
                .addChildEventListener(listener);
    }


    private void removeChildEventListener() {
        FirebaseDatabase.getInstance().getReference()
                .child("GroupMembers")
                .child(currentUserId)
                .removeEventListener(listener);
    }


    private void removeGroup(String groupId) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).group_id.equals( groupId )) {
                groups.remove(i);
                adapterRecGroups.notifyItemRemoved(i);
                break;
            }
        }
    }


    private void addGroup(String groupId) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Groups")
                .child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        groups.add( group );

                        adapterRecGroups.notifyItemInserted(groups.size() - 1);
                        adapterRecGroups.notifyItemRangeChanged(groups.size() - 1,
                                groups.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    @Override
    public void onDetach() {
        super.onDetach();
        removeChildEventListener();
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}