package com.settlex.android.ui.dashboard.rewards;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentDashboardRewardsBinding;
import com.settlex.android.ui.dashboard.home.CommissionWithdrawalActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RewardsDashboardFragment extends Fragment {
    private FragmentDashboardRewardsBinding binding;
    private UserViewModel userViewModel;

    public RewardsDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardRewardsBinding.inflate(inflater, container, false);

        observeUserAuthState();
        setupUiActions();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // observers
    private void observeUserAuthState() {
        userViewModel.getAuthStateLiveData().observe(getViewLifecycleOwner(), auth -> {
            if (auth == null) {
                showLoggedOutView();
                return;
            }
            observeUserDataStatus();
            binding.loggedOutState.setVisibility(View.GONE);
            binding.loggedInState.setVisibility(View.VISIBLE);
        });
    }

    private void showLoggedOutView() {
        binding.getRoot().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
        binding.headerTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        binding.loggedInState.setVisibility(View.GONE);
        binding.loggedOutState.setVisibility(View.VISIBLE);
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(requireActivity(), user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataStatusSuccess(user.getData());
                case ERROR -> {
                    // TODO: handle error
                }
            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        binding.commissionBalance.setText(StringUtil.formatToNaira(user.getCommissionBalance()));
        binding.referralCode.setText((user.getPaymentId() != null) ? user.getPaymentId() : "Get Referral Code");
        binding.totalReferralEarning.setText(StringUtil.formatToNaira(user.getReferralBalance()));
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.blue_400);
        styleText();

        binding.btnCopy.setOnClickListener(v -> copyReferralCodeToClipboard(binding.referralCode.getText().toString()));
        binding.btnShareInvitationLink.setOnClickListener(v -> showToast("Feature not yet Implemented"));
        binding.btnViewCommissionBalance.setOnClickListener(v -> navigateToActivity(CommissionWithdrawalActivity.class));
    }

    private void showToast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void navigateToActivity(Class<?> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void copyReferralCodeToClipboard(String textToCopy) {
        StringUtil.copyToClipboard(requireContext(), "Referral Code", textToCopy, true);
    }

    private void styleText() {
        String htmlText = "Get <font color='#0044CC'><b>1% commission</b></font> on every transaction your referrals make, for " +
                "<font color='#0044CC'><b>a lifetime</b></font>. Start sharing and watch your rewards grow!";
        binding.txtReferralInfo.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }
}