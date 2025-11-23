package com.settlex.android.ui.dashboard.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.FragmentDashboardAccountBinding;
import com.settlex.android.ui.auth.login.LoginActivity;
import com.settlex.android.ui.dashboard.home.ProfileActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.ui.common.state.UiState;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountDashboardFragment extends Fragment {

    // dependencies
    private FragmentDashboardAccountBinding binding;
    private UserViewModel userViewModel;

    public AccountDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardAccountBinding.inflate(inflater, container, false);

        setupUIActions();
        observeUserData();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupUIActions() {
        StatusBar.setColor(requireActivity(), R.color.blue_200);

        binding.btnProfilePic.setOnClickListener(view -> navigateToActivity(ProfileActivity.class));
        binding.btnSettings.setOnClickListener(view -> navigateToActivity(SettingsActivity.class));
        binding.btnSettingsHeader.setOnClickListener(view -> navigateToActivity(SettingsActivity.class));
        binding.btnEarnRewards.setOnClickListener(view -> navigateToFragment(R.id.rewards_fragment));
        binding.btnAbout.setOnClickListener(view -> navigateToActivity(AboutActivity.class));
        binding.btnTransactions.setOnClickListener(view -> StringFormatter.showNotImplementedToast(requireContext()));

        binding.btnSignOut.setOnClickListener(view -> {
            userViewModel.signOut();
            navigateToActivity(LoginActivity.class);
        });
    }

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == UiState.Status.SUCCESS) {
                UserUiModel user = result.data;

                ProfileService.loadProfilePic(user.photoUrl, binding.btnProfilePic);
                binding.fullName.setText(user.getFullName());
            }
        });
    }

    private void navigateToActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void navigateToFragment(@IdRes int navId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(navId);
    }
}