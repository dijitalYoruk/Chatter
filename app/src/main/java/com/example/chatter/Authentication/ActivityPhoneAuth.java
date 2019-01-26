package com.example.chatter.Authentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatter.Main.MainActivity;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

public class ActivityPhoneAuth extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    // properties
    private EditText etPhoneNumber;
    private ConstraintLayout authRootLayout;
    private FrameLayout authContainerLayout;
    private ProgressBar progressBarAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        isUserAuthenticated();
        setupToolbar();
        setupViews();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }


    private void setupViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        authRootLayout = findViewById(R.id.authRootLayout);
        authContainerLayout = findViewById(R.id.authContainerLayout);
        progressBarAuth = findViewById(R.id.progressBarAuth);
    }


    private void isUserAuthenticated() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_auth);
        toolbar.setTitle("Log In");
        setSupportActionBar(toolbar);
    }


    public void sendVerificationCode(final View view) {
        final String phoneNumber = etPhoneNumber.getText().toString();
        progressBarAuth.setVisibility( View.VISIBLE );
        view.setEnabled(false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)

                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                        // setting up views visibility for fragment.
                        authRootLayout.setVisibility(View.GONE);
                        authContainerLayout.setVisibility(View.VISIBLE);

                        // replacing fragment.
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.authContainerLayout, new FragmentPhoneVerification());
                        transaction.addToBackStack("ADD FRAG PHONE VERIFICATION");
                        transaction.commit();

                        // sending credential data to fragment.
                        EventBus.getDefault().postSticky(new EventBusDataEvent.SendPhoneAuthCredential(
                                phoneAuthCredential,
                                phoneNumber));

                        // hiding progressbar
                        progressBarAuth.setVisibility( View.INVISIBLE );
                        view.setEnabled(true);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        showToast(e.getLocalizedMessage());

                        // hiding progressbar
                        progressBarAuth.setVisibility( View.INVISIBLE );
                        view.setEnabled(true);
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            authRootLayout.setVisibility(View.VISIBLE);
    }

}