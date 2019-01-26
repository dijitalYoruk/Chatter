package com.example.chatter.Main.FragmentChats;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatter.Modals.LastMessage;
import com.example.chatter.Modals.Message;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


public class ActivityChat extends AppCompatActivity {

    // properties
    private View chatContainerLayout;
    private View chatRootLayout;
    private TextView tvUsername;
    private TextView tvStatus;
    private EditText etMessage;
    private RecyclerView recMessages;
    private ImageView imgProfile;
    private ImageView imgGetBack;
    private Toolbar toolbarChat;
    private AdapterRecChat adapter;
    private ChildEventListener listener;
    private ArrayList<Message> messages;
    private String currentUserId;
    private User contactUser;

    // static properties
    public static boolean isActivityChatOpen = false;
    public static String contactId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // getting contact data.
        contactId = getIntent().getStringExtra("contactId");
        messages = new ArrayList<>();
        getContactData();

        // getting corresponding views
        tvUsername = findViewById(R.id.tvUsername);
        tvStatus = findViewById(R.id.tvStatus);
        etMessage = findViewById(R.id.etMessage);
        recMessages = findViewById(R.id.recMessages);
        imgProfile = findViewById(R.id.imgProfile);
        imgGetBack = findViewById(R.id.imgGetBack);
        toolbarChat = findViewById(R.id.toolbar_chat);
        chatRootLayout = findViewById(R.id.chatRootLayout);
        chatContainerLayout = findViewById(R.id.chatContainerLayout);

        setRecyclerView();
        setChildEventListener();
    }


    private void getContactData() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(contactId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        contactUser = dataSnapshot.getValue(User.class);
                        loadToolbar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setRecyclerView() {
        // setting adapter.
        adapter = new AdapterRecChat(this, messages);
        recMessages.setAdapter(adapter);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recMessages.setLayoutManager(linearLayoutManager);
    }


    private void setChildEventListener() {

        listener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add( message );

                // positioning recycler view.
                adapter.notifyItemInserted(messages.size() - 1);
                adapter.notifyItemRangeChanged(messages.size() - 1, messages.size());
                recMessages.smoothScrollToPosition(messages.size() - 1);
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


    private void loadToolbar() {
        tvUsername.setText( contactUser.username );
        tvStatus.setText( contactUser.status );
        if (!contactUser.image_URL.equals(""))
            Picasso.get().load( contactUser.image_URL ).into( imgProfile );

        imgGetBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbarChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFragmentContact();
            }
        });
    }


    private void goToFragmentContact() {
        // setting up views visibility for fragment.
        chatRootLayout.setVisibility(View.GONE);
        chatContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chatContainerLayout, new FragmentContact());
        transaction.addToBackStack("ADD FRAG CONTACT");
        transaction.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        contactId = getIntent().getStringExtra("contactId");
        currentUserId = FirebaseAuth.getInstance().getUid();
        isActivityChatOpen = true;
        messages.clear();

        if (adapter != null)
            adapter.notifyDataSetChanged();

        addChildEventListener();
        updateLastMessage();
    }


    @Override
    protected void onStop() {
        super.onStop();
        isActivityChatOpen = false;
        contactId = "";
        removeChildEventListener();
        updateLastMessage();
    }


    private void updateLastMessage() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child("LastMessages")
                .child(currentUserId)
                .child(contactId)
                .child("last_message_is_seen")
                .setValue(true);
    }


    private void addChildEventListener() {
        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(currentUserId)
                .child(contactId)
                .addChildEventListener(listener);
    }


    private void removeChildEventListener() {
        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(currentUserId)
                .child(contactId)
                .removeEventListener(listener);
    }


    public void sendMessage(View view) {

        if (!etMessage.getText().toString().equals("")) {

            // constructing message
            String messageOwner = FirebaseAuth.getInstance().getUid();
            String messageContent = etMessage.getText().toString();
            String messageDate = System.currentTimeMillis() + "";
            String messageId = FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(messageOwner)
                    .child(contactId)
                    .push()
                    .getKey();

            Message message = new Message(messageContent, messageId,
                    messageDate, messageOwner);

            // saving message to database.
            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(messageOwner)
                    .child(contactId)
                    .child(messageId)
                    .setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(contactId)
                    .child(messageOwner)
                    .child(messageId)
                    .setValue(message);


            // saving as last message for contact user.
            LastMessage lastMessage = new LastMessage(messageId,
                    false);
            FirebaseDatabase.getInstance().getReference()
                    .child("LastMessages")
                    .child(contactUser.user_id)
                    .child(messageOwner)
                    .setValue(lastMessage);

            etMessage.setText("");
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        chatRootLayout.setVisibility(View.VISIBLE);

    }


    public User getContactUser() {
        return contactUser;
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


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}