package com.settlex.android.presentation.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.settlex.android.R;
import com.settlex.android.data.local.AppPrefs;
import com.settlex.android.databinding.ActivityOnboardingBinding;
import com.settlex.android.presentation.auth.login.LoginActivity;
import com.settlex.android.presentation.auth.register.RegisterActivity;
import com.settlex.android.presentation.onboarding.adapter.OnboardingAdapter;
import com.settlex.android.util.ui.StatusBar;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

/**
 * Handles onboarding flow:
 * - Displays intro screens via ViewPager2
 * - Tracks onboarding completion in preferences
 */
@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {
    @Inject
    AppPrefs prefs;
    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StatusBar.setColor(this, R.color.white);
        setupViewPager();
        setupUiActions();
    }

    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        binding.introViewPager.setAdapter(adapter);
        setupIndicatorView(binding.introViewPager);
    }

    private void setupIndicatorView(ViewPager2 viewPager2) {
        binding.roundRectIndicator.setSliderWidth(getResources().getDimension(R.dimen.page_indicator_width));
        binding.roundRectIndicator.setSliderHeight(getResources().getDimension(R.dimen.page_indicator_height));
        binding.roundRectIndicator.setupWithViewPager(viewPager2);
    }

    private void setupUiActions() {
        attachNavigation(binding.btnCreateAccount, RegisterActivity.class);
        attachNavigation(binding.btnLogin, LoginActivity.class);
        attachNavigation(binding.btnSkip, RegisterActivity.class);
    }

    private void attachNavigation(View button, Class<? extends AppCompatActivity> targetActivity) {
        button.setOnClickListener(v -> {
            prefs.setIntroViewed(true);
            startActivity(new Intent(this, targetActivity));
            finish();
        });
    }
}
