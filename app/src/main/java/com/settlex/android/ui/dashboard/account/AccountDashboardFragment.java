package com.settlex.android.ui.dashboard.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.settlex.android.SettleXApp;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.FragmentDashboardAccountBinding;
import com.settlex.android.ui.auth.activity.LoginActivity;
import com.settlex.android.ui.dashboard.home.ProfileActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity;
import com.settlex.android.utils.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountDashboardFragment extends Fragment {
    private final String TAG = AccountDashboardFragment.class.getSimpleName();

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
        observeUserDataStatus();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // clear resources
        binding = null;
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataStatusSuccess(user.getData());
                case FAILURE -> onUserDataStatusError(user.getError());
            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        ProfileService.loadProfilePic(user.getPhotoUrl(), binding.btnProfilePic);
        binding.fullName.setText(user.getFullName());
    }

    private void onUserDataStatusError(String error) {
        Log.e(TAG, "User data error: " + error);
    }

    private void setupUIActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.blue_400);

        binding.btnProfilePic.setOnClickListener(view -> navigateToActivity(ProfileActivity.class));
        binding.btnSettings.setOnClickListener(view -> navigateToActivity(SettingsActivity.class));
        binding.btnSettingsHeader.setOnClickListener(view -> navigateToActivity(SettingsActivity.class));
        binding.btnEarnRewards.setOnClickListener(view -> navigateToFragment(R.id.rewardsFragment));
        binding.btnTermsAndCondition.setOnClickListener(view -> navigateToActivity(TermsAndConditionsActivity.class));
        binding.btnPrivacyPolicy.setOnClickListener(view -> navigateToActivity(PrivacyPolicyActivity.class));
        binding.btnSignOut.setOnClickListener(view -> {
            userViewModel.signOut();
            navigateToActivity(LoginActivity.class);
        });
        binding.appVersion.setText(getAppVersion());
    }

    private String getAppVersion() {
        Context context = SettleXApp.getAppContext();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return "Version: " + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException: " + e.getMessage(), e);
        }
        return "Version: N/A";
    }

    private void navigateToActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void navigateToFragment(@IdRes int navId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(navId);
    }
}