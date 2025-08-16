package com.settlex.android.ui.Onboarding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.data.local.OnboardingPrefs;
import com.settlex.android.databinding.ActivityOnboardingBinding;
import com.settlex.android.ui.Onboarding.adapter.OnboardingAdapter;
import com.settlex.android.ui.auth.activity.SignUpActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingPrefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new OnboardingPrefs(this);

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
            prefs.setIntroViewed(true);
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            prefs.setIntroViewed(true);
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        binding.btnSkip.setOnClickListener(v -> {
            prefs.setIntroViewed(true);
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}