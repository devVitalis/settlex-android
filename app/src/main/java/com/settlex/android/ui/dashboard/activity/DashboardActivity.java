package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;
import com.settlex.android.ui.dashboard.fragments.AccountDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.HomeDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.RewardsDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.ServicesDashboardFragment;

public class DashboardActivity extends AppCompatActivity {
    private final Fragment homeFragment = new HomeDashboardFragment();
    private final Fragment servicesFragment = new ServicesDashboardFragment();
    private final Fragment rewardsFragment = new RewardsDashboardFragment();
    private final Fragment accountFragment = new AccountDashboardFragment();
    private Fragment activeFragment = homeFragment;

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        disableItemColorTint();
        if (savedInstanceState == null) setupFragments();

        // BottomNavigation listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                navigateToFragment(homeFragment);
            } else if (item.getItemId() == R.id.services) {
                navigateToFragment(servicesFragment);
            } else if (item.getItemId() == R.id.rewards) {
                navigateToFragment(rewardsFragment);
            } else if (item.getItemId() == R.id.account) {
                navigateToFragment(accountFragment);
            }
            return true;
        });
    }

    // ============================= UTILITIES ============================
    private void disableItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }

    // Add all fragments once, then hide/showPayConfirmation
    private void setupFragments() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, accountFragment, "account")
                .hide(accountFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, rewardsFragment, "rewards")
                .hide(rewardsFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, servicesFragment, "services")
                .hide(servicesFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();

        activeFragment = homeFragment;
    }

    // Switch between fragments without recreating them
    private void navigateToFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(fragment)
                    .commit();
            activeFragment = fragment;
        }
    }
}
