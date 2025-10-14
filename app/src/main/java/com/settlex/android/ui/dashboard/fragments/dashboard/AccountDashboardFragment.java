package com.settlex.android.ui.dashboard.fragments.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.settlex.android.ui.dashboard.activity.ProfileActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountDashboardFragment extends Fragment {

    private FragmentDashboardAccountBinding binding;
    private UserViewModel userViewModel;

    // Instance var
    private final String TAG = AccountDashboardFragment.class.getSimpleName();


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

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataSuccess(user.getData());
                case ERROR -> onUserDataError(user.getMessage());
            }
        });
    }

    private void onUserDataSuccess(UserUiModel user) {
        ProfileService.loadProfilePic(user.getProfileUrl(), binding.btnProfilePic);
        binding.fullName.setText(user.getFullName());
        observeAndLoadUserPrefs(user.getBalance(), user.getCommissionBalance());
    }

    private void onUserDataError(String error) {
        Log.e(TAG, "User data error: " + error);
    }

    private void observeAndLoadUserPrefs(long userBalance, long userCommBalance) {
        userViewModel.getIsBalanceHiddenLiveData().observe(getViewLifecycleOwner(), isBalanceHidden -> {
            long TOTAL_BALANCE = userBalance + userCommBalance;

            binding.btnBalanceToggle.setImageResource((isBalanceHidden) ? R.drawable.ic_visibility_off_filled : R.drawable.ic_visibility_on_filled);
            binding.totalBalance.setText((isBalanceHidden) ? StringUtil.setAsterisks() : StringUtil.formatToNaira(TOTAL_BALANCE));
        });
    }

    private void setupUIActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.gray_light);

        binding.btnProfilePic.setOnClickListener(view -> navigateToActivity(ProfileActivity.class));
        binding.btnBalanceToggle.setOnClickListener(view -> userViewModel.toggleBalanceVisibility());

        binding.btnEarnRewards.setOnClickListener(view -> navigateToFragment(R.id.rewardsFragment));
        binding.btnSignOut.setOnClickListener(view -> userViewModel.signOut());
    }


    private void navigateToActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void navigateToFragment(@IdRes int navId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(navId);
    }
}