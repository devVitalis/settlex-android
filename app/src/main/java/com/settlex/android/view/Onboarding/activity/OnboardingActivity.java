package com.settlex.android.view.Onboarding.activity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.data.local.UserOnboardingPrefs;
import com.settlex.android.databinding.ActivityOnboardingBinding;
import com.settlex.android.view.Onboarding.adapter.OnboardingAdapter;
import com.settlex.android.view.activities.SignInActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private UserOnboardingPrefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new UserOnboardingPrefs(this);

        setupStatusBar();
        setupViewPager();
        setupUIActions();
    }

    /*------------------------------------
    Setup onboarding ViewPager2 and dots
    -------------------------------------*/
    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.viewPager);
    }

    /*------------------------------
    Setup & Handle Event Listeners
    -------------------------------*/
    private void setupUIActions() {
        binding.btnCreateAccount.setOnClickListener(v -> {
            prefs.setIntroViewed();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            prefs.setIntroViewed();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        });

        binding.txtSkip.setOnClickListener(v -> {
            prefs.setIntroViewed();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /*--------------------------
    Customize status bar color
    --------------------------*/
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decor.setSystemUiVisibility(flags);
    }
}