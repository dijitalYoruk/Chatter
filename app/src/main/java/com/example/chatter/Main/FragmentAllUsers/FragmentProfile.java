package com.example.chatter.Main.FragmentAllUsers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.example.chatter.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FragmentProfile extends Fragment {

    // properties
    private View mainView;
    private ImageView imgProfile;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvPhoneNumber;
    private Button btnMakeChatRequest;
    private Button btnAcceptChatRequest;
    private Button btnRemoveChatRequest;
    private Button btnRemoveContact;
    private Button btnCancelChatRequest;
    private ProgressBar progressBar;
    private User chosenUser;

    private ValueEventListener valueEventListener1;
    private ValueEventListener valueEventListener2;
    private ValueEventListener valueEventListener3;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_profile, container, false);
        setupViews();
        setValueEventListeners();
        addValueEventListeners();
        setOnClickListeners();
        loadProfile();
        return mainView;
    }


    private void setupViews() {
        // getting the views
        imgProfile    = mainView.findViewById(R.id.imgProfile);
        tvUsername    = mainView.findViewById(R.id.tvUsername);
        tvStatus      = mainView.findViewById(R.id.tvStatus);
        tvPhoneNumber = mainView.findViewById(R.id.tvPhoneNumber);
        progressBar   = mainView.findViewById(R.id.progressBar);
        btnRemoveContact = mainView.findViewById(R.id.btnRemoveContact);
        btnMakeChatRequest   = mainView.findViewById(R.id.btnMakeChatRequest);
        btnAcceptChatRequest = mainView.findViewById(R.id.btnAcceptChatRequest);
        btnRemoveChatRequest = mainView.findViewById(R.id.btnRemoveChatRequest);
        btnCancelChatRequest = mainView.findViewById(R.id.btnCancelChatRequest);
    }


    private void setValueEventListeners() {

        // checking whether having chosen user as a contact.
        valueEventListener1 = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // already having as a contact.
                if (dataSnapshot.exists()) {
                    btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                    btnCancelChatRequest.setVisibility(View.INVISIBLE);
                    btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                    btnMakeChatRequest.setVisibility(View.INVISIBLE);
                    btnRemoveContact.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        };

        // checking whether having a request of the chosen user.
        valueEventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // having a request from the chosen user.
                if (dataSnapshot.exists()) {
                    btnAcceptChatRequest.setVisibility(View.VISIBLE);
                    btnCancelChatRequest.setVisibility(View.VISIBLE);
                    btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                    btnMakeChatRequest.setVisibility(View.INVISIBLE);
                    btnRemoveContact.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        };

        // checking whether chat request is already done.
        valueEventListener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                    btnCancelChatRequest.setVisibility(View.INVISIBLE);
                    btnRemoveChatRequest.setVisibility(View.VISIBLE);
                    btnMakeChatRequest.setVisibility(View.INVISIBLE);
                    btnRemoveContact.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        };
    }


    private void addValueEventListeners() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        // checking whether having chosen user as a contact.
        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(currentUserId)
                .orderByKey()
                .equalTo(chosenUser.user_id)
                .addValueEventListener(valueEventListener1);

        // checking whether having a request of the chosen user.
        FirebaseDatabase.getInstance().getReference()
                .child("ChatRequests")
                .child(currentUserId)
                .orderByKey()
                .equalTo(chosenUser.user_id)
                .addValueEventListener(valueEventListener2);

        // checking whether chat request is already done.
        FirebaseDatabase.getInstance().getReference()
                .child("ChatRequests")
                .child(chosenUser.user_id)
                .orderByKey()
                .equalTo(currentUserId)
                .addValueEventListener(valueEventListener3);
    }



    private void setOnClickListeners() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        // MAKE CHAT REQUEST
        btnMakeChatRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child("ChatRequests")
                        .child(chosenUser.user_id)
                        .child(currentUserId)
                        .setValue(currentUserId)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                                    btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                    btnRemoveChatRequest.setVisibility(View.VISIBLE);
                                    btnMakeChatRequest.setVisibility(View.INVISIBLE);
                                    btnRemoveContact.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
            }
        });


        // REMOVE CHAT REQUEST
        btnRemoveChatRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child("ChatRequests")
                        .child(chosenUser.user_id)
                        .child(currentUserId)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                                    btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                    btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                                    btnMakeChatRequest.setVisibility(View.VISIBLE);
                                    btnRemoveContact.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
            }
        });


        // CANCEL CHAT REQUEST
        btnCancelChatRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // checking whether having a request of the chosen user.
                FirebaseDatabase.getInstance().getReference()
                        .child("ChatRequests")
                        .child(currentUserId)
                        .child(chosenUser.user_id)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                                btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                                btnMakeChatRequest.setVisibility(View.VISIBLE);
                                btnRemoveContact.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        });

        // ACCEPT CHAT REQUEST
        btnAcceptChatRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child("ChatRequests")
                        .child(currentUserId)
                        .child(chosenUser.user_id)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                createContact();
                                btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                                btnCancelChatRequest.setVisibility(View.INVISIBLE);
                                btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                                btnMakeChatRequest.setVisibility(View.INVISIBLE);
                                btnRemoveContact.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        btnRemoveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeContact();
                removeChat();
                btnAcceptChatRequest.setVisibility(View.INVISIBLE);
                btnCancelChatRequest.setVisibility(View.INVISIBLE);
                btnRemoveChatRequest.setVisibility(View.INVISIBLE);
                btnMakeChatRequest.setVisibility(View.VISIBLE);
                btnRemoveContact.setVisibility(View.INVISIBLE);
            }
        });

    }


    private void loadProfile() {
        tvUsername.setText(chosenUser.username);
        tvPhoneNumber.setText(chosenUser.phoneNumber);
        UniversalImageLoader.setImage(chosenUser.image_URL,
                imgProfile, progressBar);

        if (!chosenUser.status.equals(""))
            tvStatus.setText(chosenUser.status);
    }


    private void removeChat() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(chosenUser.user_id)
                .child(currentUserId)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(currentUserId)
                .child(chosenUser.user_id)
                .removeValue();
    }


    private void removeContact() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(currentUserId)
                .child(chosenUser.user_id)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(chosenUser.user_id)
                .child(currentUserId)
                .removeValue();
    }


    private void createContact() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(currentUserId)
                .child(chosenUser.user_id)
                .setValue(chosenUser.user_id);

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(chosenUser.user_id)
                .child(currentUserId)
                .setValue(currentUserId);
    }


    @Subscribe(sticky = true)
    void getUserData(EventBusDataEvent.SendUserData UserData) {
        chosenUser = UserData.getUser();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
        FirebaseDatabase.getInstance().getReference().removeEventListener(valueEventListener1);
        FirebaseDatabase.getInstance().getReference().removeEventListener(valueEventListener2);
        FirebaseDatabase.getInstance().getReference().removeEventListener(valueEventListener3);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}