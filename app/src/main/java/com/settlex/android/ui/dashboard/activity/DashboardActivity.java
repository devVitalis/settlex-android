package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityDashboardBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        disableNavItemColorTint();

        /*
         NavController navController = Navigation.findNavController(this, R.id.dashboard_nav_host);
         NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
         **/

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.dashboard_nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        }
    }

    private void disableNavItemColorTint() {
        binding.bottomNavigationView.setItemIconTintList(null);
    }
}