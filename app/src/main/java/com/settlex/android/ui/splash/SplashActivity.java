package com.settlex.android.ui.splash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.data.local.OnboardingPrefs;
import com.settlex.android.ui.Onboarding.activity.OnboardingActivity;
import com.settlex.android.ui.auth.activity.PinLoginActivity;
import com.settlex.android.ui.dashboard.activity.DashboardActivity;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Handles app cold-start routing with splash screen animation.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {
    private boolean keepSplashVisible = true;
    private UserViewModel userViewModel;
    private boolean isUserLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeUserAuthState();

        // Configure splash screen retention
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashVisible);

        // Route after minimal delay (500ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashVisible = false;
            routeToDestination();
        }, 100);
    }

    private void observeUserAuthState() {
        userViewModel.getAuthStateLiveData().observe(this, authState ->
                this.isUserLoggedIn = authState != null);
    }

    private void routeToDestination() {
        OnboardingPrefs prefs = new OnboardingPrefs(this);

        Class<? extends Activity> destination =
                !prefs.isIntroViewed() ? OnboardingActivity.class :
                        isUserLoggedIn ? DashboardActivity.class :
                                PinLoginActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}