package com.example.chatter.Main;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chatter.Authentication.ActivityPhoneAuth;
import com.example.chatter.Main.FragmentAllUsers.FragmentAllUsers;
import com.example.chatter.Main.FragmentAllUsers.FragmentProfile;
import com.example.chatter.Main.FragmentChats.ActivityChat;
import com.example.chatter.Main.FragmentGroups.ActivityGroupChat;
import com.example.chatter.Main.FragmentGroups.FragmentCreateGroup;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    // static values
    public static boolean isChatTabOpen = false;
    public static boolean isGroupsTabOpen = false;

    // properties
    private View mainContainerLayout;
    private View mainRootLayout;
    private FirebaseAuth.AuthStateListener mAuth;
    private FragmentSettings fragmentSettings;
    private FragmentCreateGroup fragmentCreateGroup;
    private TabLayout tabLayout;
    private String currentUserId =
            FirebaseAuth.getInstance().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupTabLayout();
        checkNotification();
        setupViews();
        setupAuthStateListener();
        setupToolbar();
        getSupportFragmentManager().addOnBackStackChangedListener(this);

    }


    private void setupViews() {
        mainContainerLayout = findViewById(R.id.mainContainerLayout);
        mainRootLayout = findViewById(R.id.mainRootLayout);
        fragmentSettings = new FragmentSettings();
        fragmentCreateGroup = new FragmentCreateGroup();
    }


    private void checkNotification() {

        if (getIntent().getExtras() != null) {
            String notificationType = getIntent().getStringExtra("type");

            // Request Notification
            if (notificationType.equals("Request")) {
                String contactId = getIntent().getStringExtra("contact");

                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(contactId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User contactUser = dataSnapshot.getValue(User.class);
                                goToProfileFragment(contactUser);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                showToast(databaseError.getMessage());
                            }

                        });
            }

            // New Message notification
            else if (notificationType.equals("new_message")) {
                String contactId = getIntent().getStringExtra("contact");
                Intent intent = new Intent(getApplicationContext(), ActivityChat.class);
                intent.putExtra("contactId", contactId);
                startActivity(intent);
            }

            // New Group notification
            else if (notificationType.equals("new_group")) {
                tabLayout.getTabAt(1).select();
            }

            // New Group Message Notification
            else if (notificationType.equals("new_group_message")) {
                Intent intent = new Intent(getApplicationContext(), ActivityGroupChat.class);
                String groupId = getIntent().getStringExtra("groupId");
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }

        }
    }


    private void setupTabLayout() {

        // setting up tab layout.
        tabLayout = findViewById( R.id.tabBar );
        TabBarAdapter tabBarAdapter = new TabBarAdapter( getSupportFragmentManager() );

        // setting up corresponding view pager.
        ViewPager viewPager = findViewById( R.id.viewPager );
        viewPager.setAdapter( tabBarAdapter );
        tabLayout.setupWithViewPager( viewPager );
        isChatTabOpen = true;

        for (int i = 0; i < tabBarAdapter.getCount(); i++)
            tabLayout.getTabAt(i).setText( tabBarAdapter.getPageTitle(i) );

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isChatTabOpen = tab.getPosition() == 0;
                isGroupsTabOpen = tab.getPosition() == 1;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Chatter");
    }


    private void setupAuthStateListener() {

        mAuth = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Intent intent = new Intent(getApplicationContext(), ActivityPhoneAuth.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.options_item_log_out)
            FirebaseAuth.getInstance().signOut();

        else if (item.getItemId() == R.id.options_item_settings)
            goToSettinsFragment();

        else if (item.getItemId() == R.id.options_item_create_group)
            goToCreateGroupFragment();

        return super.onOptionsItemSelected(item);
    }


    private void goToCreateGroupFragment() {
        // setting up views visibility for fragment.
        mainRootLayout.setVisibility(View.GONE);
        mainContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainerLayout, fragmentCreateGroup);
        transaction.addToBackStack("ADD FRAG GROUPS");
        transaction.commit();
    }


    private void goToSettinsFragment() {
        // setting up views visibility for fragment.
        mainRootLayout.setVisibility(View.GONE);
        mainContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainerLayout, fragmentSettings);
        transaction.addToBackStack("ADD FRAG SETTINGS");
        transaction.commit();
    }


    public void goToProfileFragment(User contactUser) {
        // setting up views visibility for fragment.
        mainRootLayout.setVisibility(View.GONE);
        mainContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainerLayout, new FragmentProfile());
        transaction.addToBackStack("ADD FRAG CONTACT PROFILE");
        transaction.commit();

        // sending credential data to fragment.
        EventBus.getDefault().postSticky(new EventBusDataEvent.SendUserData( contactUser ));
    }


    public void goToContactsFragment() {
        // setting up views visibility for fragment.
        mainRootLayout.setVisibility(View.GONE);
        mainContainerLayout.setVisibility(View.VISIBLE);

        // replacing fragment.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainerLayout, new FragmentAllUsers());
        transaction.addToBackStack("ADD FRAG CONTACTS");
        transaction.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuth);
        setDeviceToken();
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth);
        isChatTabOpen = false;
        isGroupsTabOpen = false;
    }


    private void setDeviceToken() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .child("device_token")
                .setValue(deviceToken);
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

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .child("is_online")
                .setValue(isOnline);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId)
                .child("last_seen")
                .setValue(lastSeen);
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            mainRootLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {

            switch (requestCode){

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                    fragmentCreateGroup.setGroupProfile(data);
                    fragmentSettings.updateProfileImage(data);
                } break;

            }
        }
    }

    public void refreshCreateGroupFragment() {
        fragmentCreateGroup = new FragmentCreateGroup();
    }
}