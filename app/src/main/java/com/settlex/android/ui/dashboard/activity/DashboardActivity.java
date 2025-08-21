package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;
import com.settlex.android.ui.dashboard.fragments.AccountDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.HomeDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.RewardsDashboardFragment;
import com.settlex.android.ui.dashboard.fragments.ServicesDashboardFragment;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        disableItemColorTint();

        // Load default fragment
        if (savedInstanceState == null) navigateToFragment(new HomeDashboardFragment());

        // Navigation bar listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                navigateToFragment(new HomeDashboardFragment());

            } else if (item.getItemId() == R.id.services) {
                navigateToFragment(new ServicesDashboardFragment());

            } else if (item.getItemId() == R.id.rewards) {
                navigateToFragment(new RewardsDashboardFragment());

            } else if (item.getItemId() == R.id.account) {
                navigateToFragment(new AccountDashboardFragment());
            }
            return true;
        });

    }

    // ============================= UTILITIES ============================
    private void disableItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }

    private void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
