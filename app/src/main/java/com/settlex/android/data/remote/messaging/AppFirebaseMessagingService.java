package com.settlex.android.data.remote.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.settlex.android.R;
import com.settlex.android.data.repository.AuthRepository;

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

        // handle data payload
        if (!message.getData().isEmpty()) {
            handleDataMessageAndTiggerNotification(message.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        authRepository.storeNewToken(token);
    }

    private void handleDataMessageAndTiggerNotification(Map<String, String> data) {
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_circle)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        manager.notify(new Random().nextInt(), builder.build());
    }
}
