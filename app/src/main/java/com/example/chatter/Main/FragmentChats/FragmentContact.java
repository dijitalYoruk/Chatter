package com.example.chatter.Main.FragmentChats;

import android.content.Intent;
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

import com.example.chatter.Main.MainActivity;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FragmentContact extends Fragment {

    // properties
    private View mainView;
    private ImageView imgProfile;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvPhoneNumber;
    private Button btnRemoveContact;
    private ProgressBar progressBar;
    private User contactUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_contact, container, false);
        setupViews();
        setOnClickListeners();
        loadProfile();
        return mainView;
    }


    private void setupViews() {
        imgProfile    = mainView.findViewById(R.id.imgProfile);
        tvUsername    = mainView.findViewById(R.id.tvUsername);
        tvStatus      = mainView.findViewById(R.id.tvStatus);
        tvPhoneNumber = mainView.findViewById(R.id.tvPhoneNumber);
        progressBar   = mainView.findViewById(R.id.progressBar);
        btnRemoveContact = mainView.findViewById(R.id.btnRemoveContact);
    }


    private void setOnClickListeners() {
        btnRemoveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeContact();
                removeChat();
                goToMainActivity();
            }
        });
    }


    private void goToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void loadProfile() {
        contactUser = ((ActivityChat)getActivity()).getContactUser();
        tvUsername.setText(contactUser.username);
        tvPhoneNumber.setText(contactUser.phoneNumber);
        UniversalImageLoader.setImage(contactUser.image_URL,
                imgProfile, progressBar);

        if (!contactUser.status.equals(""))
            tvStatus.setText(contactUser.status);
    }


    private void removeChat() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(contactUser.user_id)
                .child(currentUserId)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(currentUserId)
                .child(contactUser.user_id)
                .removeValue();
    }


    private void removeContact() {
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(currentUserId)
                .child(contactUser.user_id)
                .removeValue();

        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(contactUser.user_id)
                .child(currentUserId)
                .removeValue();
    }
}