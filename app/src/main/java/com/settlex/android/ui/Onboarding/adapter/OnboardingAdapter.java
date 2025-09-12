package com.settlex.android.ui.Onboarding.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.settlex.android.ui.Onboarding.fragment.OnboardingPage1Fragment;
import com.settlex.android.ui.Onboarding.fragment.OnboardingPage2Fragment;
import com.settlex.android.ui.Onboarding.fragment.OnboardingPage3Fragment;
import com.settlex.android.ui.Onboarding.fragment.OnboardingPage4Fragment;


public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new OnboardingPage1Fragment();
            case 1 -> new OnboardingPage2Fragment();
            case 2 -> new OnboardingPage3Fragment();
            default -> new OnboardingPage4Fragment();
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
