package com.example.chatter.Main.FragmentGroups;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatter.Modals.Group;
import com.example.chatter.Modals.Message;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ActivityGroupChat extends AppCompatActivity {

    // properties
    private EditText etMessage;
    private RecyclerView rcvMessages;
    private View groupChatContainerLayout;
    private View groupChatRootLayout;
    private Group currentGroup;
    private User currentUser;
    private ArrayList<Message> messages;
    private AdapterRecGroupChatMes adapter;
    private ChildEventListener childEventListener;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentUserId = FirebaseAuth.getInstance().getUid();
        messages = new ArrayList<>();
        setupViews();
        getGroupData();
        getCurrentUser();
        setChildEventListener();
    }


    private void setupViews() {
        etMessage   = findViewById(R.id.etMessage);
        rcvMessages = findViewById(R.id.rcvMessages);
        groupChatRootLayout = findViewById(R.id.groupChatRootLayout);
        groupChatContainerLayout = findViewById(R.id.groupChatContainerLayout);
    }


    private void getGroupData() {
        String groupId = getIntent().getStringExtra("groupId");
        FirebaseDatabase.getInstance().getReference()
                .child("Groups")
                .child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentGroup = dataSnapshot.getValue(Group.class);
                        setupToolbar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast( databaseError.getMessage() );
                    }
                });
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_group_chat);
        toolbar.setTitle(currentGroup.group_name);
        setSupportActionBar(toolbar);
    }


    private void getCurrentUser() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
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
        adapter = new AdapterRecGroupChatMes(this, messages, currentUser);
        rcvMessages.setAdapter(adapter);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvMessages.setLayoutManager(linearLayoutManager);
    }


    private void setChildEventListener() {
        childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);

                // adding message
                messages.add(message);

                // positioning recycler view.
                adapter.notifyItemInserted(messages.size() - 1);
                adapter.notifyItemRangeChanged(messages.size() - 1, messages.size());
                rcvMessages.smoothScrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        };
    }


    @Override
    protected void onStart() {
        super.onStart();
        messages.clear();

        if (adapter != null)
            adapter.notifyDataSetChanged();

        addChildEventListener();
    }


    @Override
    protected void onStop() {
        super.onStop();
        removeChildEventListener();
    }


    private void removeChildEventListener() {
        String groupId = getIntent().getStringExtra("groupId");
        FirebaseDatabase.getInstance().getReference()
                .child("GroupChatMessages")
                .child(groupId)
                .removeEventListener(childEventListener);
    }


    private void addChildEventListener() {
        String groupId = getIntent().getStringExtra("groupId");
        FirebaseDatabase.getInstance().getReference()
                .child("GroupChatMessages")
                .child(groupId)
                .addChildEventListener(childEventListener);
    }


    public void sendMessage(View view) {
        String messageContent = etMessage.getText().toString();
        String messageDate  = System.currentTimeMillis() + "";
        String messageOwner = currentUser.username;
        String messageId = FirebaseDatabase.getInstance().getReference()
                .child("GroupChatMessages")
                .child(currentGroup.group_id)
                .push()
                .getKey();

        Message message = new Message(messageContent, messageId,
                messageDate, messageOwner);

        FirebaseDatabase.getInstance().getReference()
                .child("GroupChatMessages")
                .child(currentGroup.group_id)
                .child(messageId)
                .setValue(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            etMessage.setText("");
                        } else {
                            showToast(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }


    public void goToProfileGroup(View view) {
        // setting up views visibility for fragment.
        groupChatRootLayout.setVisibility(View.GONE);
        groupChatContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.groupChatContainerLayout, new FragmentGroup());
        transaction.addToBackStack("ADD FRAG GROUP");
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        groupChatRootLayout.setVisibility(View.VISIBLE);
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        updateUserState(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUserState(true);
    }


    private void updateUserState(boolean isOnline) {
        String lastSeen = System.currentTimeMillis() + "";
        String userId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .child("is_online")
                .setValue(isOnline);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .child("last_seen")
                .setValue(lastSeen);
    }


    public Group getCurrentGroup() {
        return currentGroup;
    }

}