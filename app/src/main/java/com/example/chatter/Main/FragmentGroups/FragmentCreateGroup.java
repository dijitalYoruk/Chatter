package com.example.chatter.Main.FragmentGroups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatter.Main.FragmentChats.ActivityChat;
import com.example.chatter.Main.MainActivity;
import com.example.chatter.Modals.Group;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.ArrayList;

import static com.example.chatter.Main.FragmentChats.ActivityChat.contactId;

public class FragmentCreateGroup extends Fragment {

    // properties
    private Button btnCreate;
    private Button btnCancel;
    private EditText etGroupName;
    private View mainView;
    private ImageView imgGroup;
    private RecyclerView rcvGroupMembers;
    private AdapterRecGroupMembers adapter;
    private ArrayList<User> contacts;
    private String userId;
    private boolean executeProfile;
    private Uri groupImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_create_group, container, false);
        userId = FirebaseAuth.getInstance().getUid();
        contacts = new ArrayList<>();
        executeProfile = false;
        getContactsData();
        setupViews();
        setOnClickListeners();
        return mainView;
    }


    private void getContactsData() {
        // getting contacts id data
        FirebaseDatabase.getInstance().getReference()
                .child("Contacts")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final int contactCount = (int) dataSnapshot.getChildrenCount();
                        for (DataSnapshot data: dataSnapshot.getChildren()) {
                            String contactId = data.getValue().toString();
                            getContactData(contactCount, contactId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getContactData(final int contactCount, String contactId) {

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(contactId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User contact = dataSnapshot.getValue(User.class);
                        contacts.add(contact);

                        if (contacts.size() == contactCount)
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
        adapter = new AdapterRecGroupMembers(getContext(), contacts);
        rcvGroupMembers.setAdapter(adapter);

        // setting layout.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvGroupMembers.setLayoutManager(linearLayoutManager);
    }


    private void setupViews() {
        btnCreate   = mainView.findViewById(R.id.btnCreate);
        btnCancel   = mainView.findViewById(R.id.btnCancel);
        etGroupName = mainView.findViewById(R.id.etGroupName);
        rcvGroupMembers = mainView.findViewById(R.id.rcvGroupMembers);
        imgGroup = mainView.findViewById(R.id.imgGroup);
    }


    private void setOnClickListeners() {

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etGroupName.getText().toString();

                if (!groupName.equals(""))
                    if (adapter.getGroupMembers().size() != 0) {
                        saveGroup(etGroupName.getText().toString());
                    }


                else // no group name.
                    showToast("Please enter group name.");
            }
        });

        imgGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeProfile = true;
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getActivity());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }


    private void saveGroup(String groupName) {
        String adminId = FirebaseAuth.getInstance().getUid();
        final String groupId = FirebaseDatabase.getInstance().getReference()
                .child("Groups").push().getKey();

        Group group = new Group(groupId, adminId, groupName, "");
        saveGroupProfileToFirebaseStorage(groupId);

        FirebaseDatabase.getInstance().getReference()
                .child("Groups")
                .child(groupId)
                .setValue(group)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            saveGroupMembers(groupId);
                    }
                });
    }


    private void saveGroupMembers(String groupId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        for (int i = 0; i < adapter.getGroupMembers().size(); i++) {
            String memberId = adapter.getGroupMembers().get(i).user_id;
            FirebaseDatabase.getInstance().getReference()
                    .child("GroupMembers")
                    .child(memberId)
                    .child(groupId)
                    .setValue(groupId);
        }

        FirebaseDatabase.getInstance().getReference()
                .child("GroupMembers")
                .child(currentUserId)
                .child(groupId)
                .setValue(groupId);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    public void setGroupProfile(Intent data) {
        if (executeProfile) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            groupImage = result.getUri();
            Picasso.get().load(groupImage).into(imgGroup);
            executeProfile = false;
        }
    }

    private void saveGroupProfileToFirebaseStorage(final String groupId) {

        if (groupImage != null) {

            final StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child("Groups")
                    .child(groupId)
                    .child("group_image");

            UploadTask uploadTask = ref.putFile(groupImage);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                        throw task.getException();

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String imageUrl = task.getResult().toString();
                        uploadGroupImageToFirebaseDatabase(imageUrl, groupId);
                    } else // error
                        showToast(task.getException().getLocalizedMessage());
                }
            });
        }

        else {
            showToast("Group Created Succesfully");
            refreshFragment();
        }
    }

    private void refreshFragment() {
        etGroupName.setText("");
        groupImage = null;
        Picasso.get().load(R.mipmap.image_asset_group).into(imgGroup);
        getActivity().onBackPressed();
    }

    private void uploadGroupImageToFirebaseDatabase(String imageUrl, String groupId) {
        FirebaseDatabase.getInstance().getReference()
                .child("Groups")
                .child(groupId)
                .child("image_URL")
                .setValue(imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showToast("Group Created Succesfully");
                            refreshFragment();
                        }
                        else // error
                            showToast(task.getException().getLocalizedMessage());
                    }
                });
    }

}