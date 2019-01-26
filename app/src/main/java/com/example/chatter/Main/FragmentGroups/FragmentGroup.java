package com.example.chatter.Main.FragmentGroups;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatter.Main.MainActivity;
import com.example.chatter.Modals.Group;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentGroup extends Fragment {

    // properties
    private View mainView;
    private ImageView imgGroupProfile;
    private TextView tvGroupName;
    private Button btnLeaveGroup;
    private Button btnCloseGroup;
    private RecyclerView recGroupMembers;
    private Group currentGroup;
    private AdapterRecMembers adapterRecMembers;
    private ArrayList<User> members;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_group, container, false);
        currentGroup = ((ActivityGroupChat) getActivity()).getCurrentGroup();
        adapterRecMembers = new AdapterRecMembers(getContext(), members, currentGroup.admin_id);
        members = new ArrayList<>();
        setupViews();
        setRecyclerView();
        getGroupMembers();
        setVisibilityOfViews();
        return mainView;
    }


    private void setupViews() {
        imgGroupProfile = mainView.findViewById(R.id.imgGroupProfile);
        tvGroupName = mainView.findViewById(R.id.tvGroupName);
        btnLeaveGroup = mainView.findViewById(R.id.btnLeaveGroup);
        btnCloseGroup = mainView.findViewById(R.id.btnCloseGroup);
        recGroupMembers = mainView.findViewById(R.id.recGroupMembers);
        tvGroupName.setText(currentGroup.group_name);
    }


    private void setRecyclerView() {
        // setting adapter.
        recGroupMembers.setAdapter(adapterRecMembers);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recGroupMembers.setLayoutManager(linearLayoutManager);
    }


    private void getGroupMembers() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("GroupMembers")
                .orderByChild(currentGroup.group_id)
                .equalTo(currentGroup.group_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data: dataSnapshot.getChildren())
                            addMember(data.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void addMember(String memberUserId) {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(memberUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        members.add( dataSnapshot.getValue(User.class) );
                        adapterRecMembers.notifyItemInserted( members.size() - 1 );
                        adapterRecMembers.notifyItemRangeChanged(members.size() - 1
                                , members.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setVisibilityOfViews() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        // being group admin
        if (currentUserId.equals( currentGroup.admin_id )) {
            btnCloseGroup.setVisibility(View.VISIBLE);
            btnLeaveGroup.setVisibility(View.INVISIBLE);

            btnCloseGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterRecMembers.closeGroup( currentGroup.group_id );
                    goToMainActivity();
                }
            });
        }

        else { // being group member.
            btnCloseGroup.setVisibility(View.INVISIBLE);
            btnLeaveGroup.setVisibility(View.VISIBLE);

            btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("GroupMembers")
                            .child(currentUserId)
                            .child(currentGroup.group_id)
                            .removeValue();

                    goToMainActivity();
                }
            });
        }
    }


    private void goToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}