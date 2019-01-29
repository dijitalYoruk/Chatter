package com.example.chatter.Main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.UniversalImageLoader;
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

public class FragmentSettings extends Fragment {

    // properties
    private ImageView imgProfile;
    private EditText etUsername;
    private EditText etStatus;
    private View mainView;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private ProgressBar progressBarProfile;
    private User currentUser;
    private Uri newProfileImageUri;
    private String currentUserId;
    private boolean executeUpdate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentUserId = FirebaseAuth.getInstance().getUid();
        mainView = inflater.inflate(R.layout.fragment_settings, container, false);
        executeUpdate = false;
        setupViews();
        loadProfile();
        setupOnClickListeners();
        return mainView;
    }


    private void setupViews() {
        etUsername = mainView.findViewById(R.id.etUsername);
        imgProfile = mainView.findViewById(R.id.imgProfile);
        etStatus = mainView.findViewById(R.id.etStatus);
        btnUpdate = mainView.findViewById(R.id.btnUpdate);
        progressBar = mainView.findViewById(R.id.progressBar);
        progressBarProfile = mainView.findViewById(R.id.progressBarProfile);
    }


    private void loadProfile() {

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);

                        if (currentUser != null) {

                            UniversalImageLoader.setImage(currentUser.image_URL,
                                    imgProfile, progressBarProfile);

                            if (!currentUser.status.equals(""))
                                etStatus.setText(currentUser.status);

                            etUsername.setText(currentUser.username);
                            makeUIVisible();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void makeUIVisible() {
        etStatus.setVisibility(View.VISIBLE);
        imgProfile.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }


    private void setupOnClickListeners() {
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileImage();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }


    public void updateProfile() {
        String updatedUsername = etUsername.getText().toString();
        String updatedStatus   = etStatus.getText().toString();

        if (!updatedStatus.equals( currentUser.status )
                || !updatedUsername.equals( currentUser.username )) {

            currentUser.username = updatedUsername;
            currentUser.status = updatedStatus;

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.user_id)
                    .setValue(currentUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                showToast("Profile is updated.");
                                getActivity().onBackPressed();
                            }

                            else showToast(task.getException().getLocalizedMessage());
                        }
                    });
        }
    }


    public void updateProfileImage() {
        executeUpdate = true;
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getActivity());
    }


    public void updateProfileImage(Intent data) {
        if (executeUpdate) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            newProfileImageUri = result.getUri();

            //Picasso.get().load(newProfileImageUri).into(imgProfile);
            UniversalImageLoader.setImage(newProfileImageUri.toString(),
                    imgProfile, progressBarProfile);

            uploadUserProfileImageToFirebaseStorage();
            executeUpdate = false;
        }
    }


    public void uploadUserProfileImageToFirebaseStorage() {

        if (newProfileImageUri != null) {

            final StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child("Users")
                    .child(currentUser.user_id)
                    .child("profile_image");

            UploadTask uploadTask = ref.putFile(newProfileImageUri);

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
                        currentUser.image_URL = task.getResult().toString();
                        uploadUserProfileImageToFirebaseDatabase();
                    } else // error
                        showToast(task.getException().getLocalizedMessage());
                }
            });

        }
    }


    private void uploadUserProfileImageToFirebaseDatabase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .child("image_URL")
                .setValue(currentUser.image_URL)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            showToast("Image Updated Successfully");
                        else // error
                            showToast(task.getException().getLocalizedMessage());
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}