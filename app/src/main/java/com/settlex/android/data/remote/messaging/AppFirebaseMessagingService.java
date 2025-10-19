package com.settlex.android.data.remote.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.settlex.android.R;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.dashboard.DashboardActivity;

import java.util.Map;
import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@AndroidEntryPoint
public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    AuthRepository authRepository;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        // Check if message contains data payload
        if (!message.getData().isEmpty()) {
            handleDataMessage(message.getData());
        }


        // If there's also a notification payload (optional)
        if (message.getNotification() != null) {
            showNotification(message.getNotification().getTitle(),
                    message.getNotification().getBody());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "settlex_channel";

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "General Notification",
                NotificationManager.IMPORTANCE_HIGH
        );
        manager.createNotificationChannel(channel);

        // Intent to open Dashboard when notification tapped
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_circle)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        manager.notify(new Random().nextInt(), builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        authRepository.sendTokenToServer(token);
    }
}
