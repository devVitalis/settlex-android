package com.settlex.android.ui.activities.splash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.settlex.android.data.local.OnboardingPrefs;
import com.settlex.android.data.local.session.SessionManager;
import com.settlex.android.ui.Onboarding.activity.OnboardingActivity;
import com.settlex.android.ui.auth.activity.PinLoginActivity;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.dashboard.activity.DashboardActivity;

/**
 * Handles app cold-start routing with splash screen animation.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private boolean keepSplashVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configure splash screen retention
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashVisible);

        super.onCreate(savedInstanceState);

        // Route after minimal delay (500ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashVisible = false;
            routeToDestination();
        }, 300);
    }

    /**
     * Determines next activity based on onboarding/auth state
     */
    private void routeToDestination() {
        SessionManager session = SessionManager.getInstance();
        OnboardingPrefs prefs = new OnboardingPrefs(this);

        Class<? extends Activity> destination =
                !prefs.isIntroViewed() ? OnboardingActivity.class :
                        !session.isUserLoggedIn() || !session.hasPin() ? DashboardActivity.class :
                                PinLoginActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}