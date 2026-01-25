package com.settlex.android.presentation.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.settlex.android.presentation.onboarding.OnboardingPage1Fragment
import com.settlex.android.presentation.onboarding.OnboardingPage2Fragment
import com.settlex.android.presentation.onboarding.OnboardingPage3Fragment
import com.settlex.android.presentation.onboarding.OnboardingPage4Fragment

class OnboardingAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingPage1Fragment()
            1 -> OnboardingPage2Fragment()
            2 -> OnboardingPage3Fragment()
            else -> OnboardingPage4Fragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}
