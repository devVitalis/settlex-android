package com.settlex.android.presentation.rewards;

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
import com.settlex.android.presentation.wallet.CommissionWithdrawalActivity;
import com.settlex.android.presentation.account.model.UserUiModel;
import com.settlex.android.presentation.account.model.UserViewModel;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;

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

//            switch (user.status) {
//                case SUCCESS -> onUserDataStatusSuccess(user.data);
//                case FAILURE -> {
//                    // TODO: handle error
//                }
//            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        binding.commissionBalance.setText(CurrencyFormatter.formatToNaira(user.commissionBalance));
        binding.referralCode.setText((user.paymentId != null) ? user.paymentId : "Get Referral Code");
        binding.totalReferralEarning.setText(CurrencyFormatter.formatToNaira(user.referralBalance));
    }

    private void setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.blue_400);
        styleText();

        binding.btnCopy.setOnClickListener(v -> copyReferralCodeToClipboard(binding.referralCode.getText().toString()));
        binding.btnShareInvitationLink.setOnClickListener(v -> showToast("Feature not yet Implemented"));
        binding.btnViewCommissionBalance.setOnClickListener(v -> navigateToActivity(CommissionWithdrawalActivity.class));
    }

    private void showToast(java.lang.String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void navigateToActivity(Class<?> activityClass) {
        startActivity(new Intent(requireContext(), activityClass));
    }

    private void copyReferralCodeToClipboard(java.lang.String textToCopy) {
        StringFormatter.copyToClipboard(requireContext(), "Referral Code", textToCopy, true);
    }

    private void styleText() {
        java.lang.String htmlText = "Get <font color='#0044CC'><b>1% commission</b></font> on every transaction your referrals make, for " +
                "<font color='#0044CC'><b>a lifetime</b></font>. Start sharing and watch your rewards grow!";
        binding.txtReferralInfo.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }
}