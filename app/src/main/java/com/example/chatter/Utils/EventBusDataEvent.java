package com.example.chatter.Utils;

import com.example.chatter.Modals.Group;
import com.example.chatter.Modals.User;
import com.google.firebase.auth.PhoneAuthCredential;

public class EventBusDataEvent {


    public static class SendPhoneAuthCredential {

        // properties
        private PhoneAuthCredential phoneAuthCredential;
        private String phoneNumber;

        // constructor
        public SendPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential, String phoneNumber) {
            this.phoneAuthCredential = phoneAuthCredential;
            this.phoneNumber = phoneNumber;
        }

        // methods

        public PhoneAuthCredential getPhoneAuthCredential() {
            return phoneAuthCredential;
        }

        public void setPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
            this.phoneAuthCredential = phoneAuthCredential;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class SendUserData {

        // properties
        private User user;

        // constructor
        public SendUserData(User user) {
            this.user = user;
        }

        // methods

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

}
