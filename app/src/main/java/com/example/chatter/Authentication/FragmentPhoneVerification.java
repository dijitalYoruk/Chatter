package com.example.chatter.Authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatter.Main.MainActivity;
import com.example.chatter.Modals.User;
import com.example.chatter.R;
import com.example.chatter.Utils.EventBusDataEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FragmentPhoneVerification extends Fragment {

    // properties
    private View mainView;
    private Button btnLogin;
    private TextView tvTimer;
    private EditText etVerificationCode;
    private ProgressBar progressBarTimer;
    private PhoneAuthCredential phoneCredential;
    private CountDownTimer countDownTimer;
    private String phoneNumber;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflating and getting required views.
        mainView = inflater.inflate(R.layout.fragment_phone_verification, container, false);
        setupViews();
        setupTimer();
        setOnClickListeners();
        return mainView;
    }


    private void setupViews() {
        etVerificationCode = mainView.findViewById(R.id.etVerificationCode);
        btnLogin = mainView.findViewById(R.id.btnLogin);
        progressBarTimer = mainView.findViewById(R.id.progressBarTimer);
        tvTimer = mainView.findViewById(R.id.tvTimer);
    }


    private void setupTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long remainingSeconds = millisUntilFinished / 1000;
                tvTimer.setText(remainingSeconds + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Time Out, Try Again");
                goBack();
            }

        }.start();
    }


    private void goBack() {
        new CountDownTimer(2000, 3000) {

            @Override
            public void onFinish() { getActivity().onBackPressed(); }

            @Override
            public void onTick(long millisUntilFinished) {}

        }.start();
    }


    private void setOnClickListeners() {

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String verificationCodeEntry = etVerificationCode.getText().toString();

                // checking whether passwords match.
                if (verificationCodeEntry.equals( phoneCredential.getSmsCode() )) {
                    progressBarTimer.setVisibility(View.VISIBLE);

                    // recording and signing in
                    FirebaseAuth.getInstance()
                            .signInWithCredential(phoneCredential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                        checkUserExistence();
                                    else // error
                                        showToast(task.getException().getLocalizedMessage());
                                }
                            });
                }
            }
        });

    }

    private void checkUserExistence() {
        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .orderByKey()
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // user already exists in db.
                        if (dataSnapshot.exists()) {
                            setDeviceToken();
                            goToMainActivity();
                            countDownTimer.cancel();
                        }

                        else // user not exist.
                            saveUserToDatabase();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast(databaseError.getMessage());
                    }
                });
    }


    private void setDeviceToken() {
        String userId = FirebaseAuth.getInstance().getUid();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .child("device_token")
                .setValue(deviceToken);
    }


    private void saveUserToDatabase() {
        // creating user
        String userId = FirebaseAuth.getInstance().getUid();
        String lastSeen = System.currentTimeMillis() + "";
        User user = new User(userId, phoneNumber, phoneNumber,
                "", "", false, lastSeen);

        // saving user to database.
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            setDeviceToken();
                            goToMainActivity();
                            countDownTimer.cancel();
                            progressBarTimer.setVisibility(View.INVISIBLE);
                        } else
                            showToast(task.getException().getLocalizedMessage());
                    }
                });
    }


    private void goToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    @Subscribe(sticky = true)
    void getCredentialData(EventBusDataEvent.SendPhoneAuthCredential credentialData) {
        phoneCredential = credentialData.getPhoneAuthCredential();
        phoneNumber = credentialData.getPhoneNumber();
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
    }

}