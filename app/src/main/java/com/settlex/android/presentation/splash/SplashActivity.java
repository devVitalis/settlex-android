package com.settlex.android.presentation.splash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.data.local.AppPrefs;
import com.settlex.android.presentation.auth.AuthViewModel;
import com.settlex.android.presentation.auth.login.LoginActivity;
import com.settlex.android.presentation.dashboard.DashboardActivity;
import com.settlex.android.presentation.onboarding.OnboardingActivity;
import com.settlex.android.util.permission.NotificationPermission;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Inject
    NotificationPermission notification;
    @Inject
    AppPrefs prefs;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure splash screen retention
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> true);

        initPermissionLauncher();
    }

    private void initPermissionLauncher() {
        notification.initNotificationLauncher(
                this::routeToDestination,
                () -> {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    routeToDestination();
                }
        );

        if (notification.shouldRequestPermission()) {
            notification.requestNotificationPermission();
        } else {
            routeToDestination();
        }
    }

    private void routeToDestination() {
        Class<? extends Activity> destination =
                (!prefs.isIntroViewed()) ? OnboardingActivity.class :
                        (authViewModel.isUserLoggedIn()) ? LoginActivity.class
                                : DashboardActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}