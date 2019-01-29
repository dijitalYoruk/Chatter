package com.example.chatter.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.example.chatter.Main.FragmentChats.ActivityChat;
import com.example.chatter.Main.FragmentGroups.ActivityGroupChat;
import com.example.chatter.Main.MainActivity;
import com.example.chatter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCM extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            // getting notification data
            String requestType = remoteMessage.getData().get("type");
            String title       = remoteMessage.getData().get("title");
            String body        = remoteMessage.getData().get("body");
            String contactId   = remoteMessage.getData().get("contact");
            String groupId     = remoteMessage.getData().get("groupId");


            switch (requestType) {

                case "Request": {
                    showRequestNotification(title, body, contactId);
                } break;

                case "new_message": {
                    if (!MainActivity.isChatTabOpen && !(ActivityChat.isActivityChatOpen && ActivityChat.contactId.equals(contactId)))
                        showNewMessageNotification(title, body, contactId);
                } break;

                case "remove": {
                    showContactRemovedNotification(title, body);
                } break;

                case "new_group": {
                    showNewGroupNotification(title, body);
                } break;

                case "new_group_message": {
                    if (!MainActivity.isGroupsTabOpen && !(ActivityGroupChat.isActivityGroupChatOpen && ActivityGroupChat.groupId.equals(groupId)))
                        showNewGroupMessageNotification(title, body, groupId);
                } break;

            }
        }
    }


    private void showNewGroupMessageNotification(String title, String body, String groupId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "new_group_message");
        intent.putExtra("groupId", groupId);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                50, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder = new NotificationCompat.Builder(this, "Ne")
                .setSmallIcon(R.drawable.icon_app )
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_app))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(generateRandomKey(groupId), notificationBuilder);
    }


    private void showNewGroupNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "new_group");

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                40, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder = new NotificationCompat.Builder(this, "Ne")
                .setSmallIcon(R.drawable.icon_app )
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_app))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder);
    }


    private void showContactRemovedNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                30, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder = new NotificationCompat.Builder(this, "Ne")
                .setSmallIcon(R.drawable.icon_app )
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_app))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder);
    }

    private void showNewMessageNotification(String title, String body, String contactId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("type", "new_message");
        intent.putExtra("contact", contactId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                20, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder = new NotificationCompat.Builder(this, "Ne")
                .setSmallIcon(R.drawable.icon_app )
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_app))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(generateRandomKey(contactId), notificationBuilder);
    }


    private void showRequestNotification(String title, String body, String contact) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "Request");
        intent.putExtra("contact", contact);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder = new NotificationCompat.Builder(this, "Ne")
                .setSmallIcon(R.drawable.icon_app )
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_app))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder);
    }


    private void setDeviceToken(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId)
                .child("device_token")
                .setValue(token);
    }


    private int generateRandomKey(String contactId) {
        int randomKey = 0;

        for (int i = 0; i < contactId.length(); i++)
            randomKey += (int)contactId.charAt(i);

        return randomKey;
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        setDeviceToken(s);
    }
}