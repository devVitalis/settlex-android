package com.settlex.android.controller;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.settlex.android.data.local.SessionManager;
import com.settlex.android.data.local.UserOnboardingPrefs;
import com.settlex.android.view.Onboarding.activity.OnboardingActivity;
import com.settlex.android.view.activities.SignInActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean keepSplashVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashVisible);

        super.onCreate(savedInstanceState);

        SessionManager sm = SessionManager.getInstance();
        UserOnboardingPrefs prefs = new UserOnboardingPrefs(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashVisible = false;

            if (!prefs.isIntroViewed()) {
                loadActivity(OnboardingActivity.class);

            } else if (!sm.isUserLoggedIn()) {
                loadActivity(SignInActivity.class);

            } else if (!sm.hasPasscode()) {
                loadActivity(SignInActivity.class);
            }
//        else {
//            // loadActivity(PasscodeLoginActivity.class);
//        }
//        finish();

        }, 500);
    }

    /*----------------------------
    Launch activity from context
    -----------------------------*/
    private void loadActivity(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}