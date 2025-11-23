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

/**
 * Handles onboarding flow:
 * - Displays intro screens via ViewPager2
 * - Tracks onboarding completion in preferences
 */
public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private AppPrefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new AppPrefs(this);

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
        binding.roundRectIndicator.setSliderWidth(getResources().getDimension(R.dimen.dp_87));
        binding.roundRectIndicator.setSliderHeight(getResources().getDimension(R.dimen.dp_5));
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
