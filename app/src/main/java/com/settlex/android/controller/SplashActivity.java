package com.settlex.android.controller;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.settlex.android.data.local.AppOnboardingPrefs;
import com.settlex.android.manager.SessionManager;
import com.settlex.android.manager.UserOnboardingPrefs;
import com.settlex.android.view.Onboarding.activity.OnboardingActivity;
import com.settlex.android.view.auth.activity.PasscodeLoginActivity;
import com.settlex.android.view.auth.activity.SignInActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean keepSplashVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashVisible);

        super.onCreate(savedInstanceState);

        SessionManager sm = SessionManager.getInstance();
        AppOnboardingPrefs prefs = new AppOnboardingPrefs(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashVisible = false;

            if (!prefs.isIntroViewed()) {
                // User hasn't seen onboarding
                loadActivity(OnboardingActivity.class);

            } else if (!sm.isUserLoggedIn() || !sm.hasPasscode()) {
                // User hasn't logged in yet || Logged in but no passcode set
                loadActivity(SignInActivity.class);

            } else {
                // User is logged in and has passcode
                loadActivity(PasscodeLoginActivity.class);
            }
        }, 500);
    }

    /*----------------------------
    Launch activity from context
    -----------------------------*/
    private void loadActivity(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}