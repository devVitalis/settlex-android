package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the single, shared ViewModel instance from the Application class.
        // userViewModel = ((SettleXApp) getApplication()).getSharedUserViewModel();
        disableItemColorTint();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dashboardNavHost);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        }
    }

    private void disableItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }
}