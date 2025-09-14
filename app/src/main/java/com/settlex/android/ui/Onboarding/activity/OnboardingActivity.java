package com.settlex.android.ui.Onboarding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;
import com.settlex.android.data.local.preference.OnboardingPrefs;
import com.settlex.android.databinding.ActivityOnboardingBinding;
import com.settlex.android.ui.Onboarding.adapter.OnboardingAdapter;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.auth.activity.SignUpActivity;
import com.settlex.android.util.ui.StatusBarUtil;

/**
 * Handles onboarding flow:
 * - Displays intro screens via ViewPager2
 * - Tracks onboarding completion in preferences
 */
public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private OnboardingPrefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new OnboardingPrefs(this);

        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupViewPager();
        setupUiActions();
    }

    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.viewPager);
    }

    private void setupUiActions() {
        attachNavigation(binding.btnCreateAccount, SignUpActivity.class);
        attachNavigation(binding.btnLogin, SignInActivity.class);
        attachNavigation(binding.btnSkip, SignUpActivity.class);
    }

    private void attachNavigation(View button, Class<? extends AppCompatActivity> targetActivity) {
        button.setOnClickListener(v -> {
            prefs.setIntroViewed(true);
            startActivity(new Intent(this, targetActivity));
            finish();
        });
    }
}
