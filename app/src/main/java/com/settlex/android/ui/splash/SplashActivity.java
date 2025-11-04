package com.settlex.android.ui.splash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.data.local.OnboardingPrefs;
import com.settlex.android.ui.auth.activity.LoginActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.onboarding.activity.OnboardingActivity;
import com.settlex.android.utils.permission.NotificationPermissionUtil;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Inject
    NotificationPermissionUtil notification;
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
        // do nothing
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
        OnboardingPrefs prefs = new OnboardingPrefs(this);

        Class<? extends Activity> destination =
                (!prefs.isIntroViewed()) ? OnboardingActivity.class :
                        LoginActivity.class;
        // (authViewModel.isUserLoggedIn()) ? LoginActivity.class : DashboardActivity.class; // TODO: set !isUserLoggedIn = DashboardActivity

        startActivity(new Intent(this, destination));
        finish();
    }
}