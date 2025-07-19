package com.settlex.android.view.Onboarding.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.settlex.android.view.Onboarding.fragment.OnboardingPage1Fragment;
import com.settlex.android.view.Onboarding.fragment.OnboardingPage2Fragment;
import com.settlex.android.view.Onboarding.fragment.OnboardingPage3Fragment;


public class OnboardingAdapter extends FragmentStateAdapter {

    /*-----------------------------------------------
    Initialize the adapter with the hosting activity
    -----------------------------------------------*/
    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /*---------------------------------------------------
    Return the correct onboarding page based on position
    0 → Page 1, 1 → Page 2, 2 → Page 3
    ---------------------------------------------------*/
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnboardingPage1Fragment();
            case 1:
                return new OnboardingPage2Fragment();
            case 2: default:
                return new OnboardingPage3Fragment();
        }
    }

    /*--------------------------------
    Total number of onboarding pages
    --------------------------------*/
    @Override
    public int getItemCount() {
        return 3;
    }
}
