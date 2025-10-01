package com.settlex.android.ui.splash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.data.local.OnboardingPrefs;
import com.settlex.android.ui.Onboarding.activity.OnboardingActivity;
import com.settlex.android.ui.auth.activity.LoginActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.dashboard.activity.DashboardActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Handles app cold-start routing with splash screen animation.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {
    private boolean keepSplashVisible = true;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure splash screen retention
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashVisible);

        // Route after minimal delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashVisible = false;
            routeToDestination();
        }, 100);
    }

    private void routeToDestination() {
        OnboardingPrefs prefs = new OnboardingPrefs(this);

        Class<? extends Activity> destination =
                (!prefs.isIntroViewed()) ? OnboardingActivity.class :
                        (authViewModel.isUserLoggedIn()) ? LoginActivity.class : DashboardActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}