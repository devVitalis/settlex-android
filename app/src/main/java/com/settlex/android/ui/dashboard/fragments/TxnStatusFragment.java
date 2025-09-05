package com.settlex.android.ui.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentTxnStatusBinding;
import com.settlex.android.ui.dashboard.viewmodel.DashboardViewModel;
import com.settlex.android.util.event.Result;

public class TxnStatusFragment extends Fragment {

    public TxnStatusFragment() {
        // Required empty public constructor
    }

    private FragmentTxnStatusBinding binding;
    private DashboardViewModel dashboardViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTxnStatusBinding.inflate(inflater, container, false);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeTxnStatus();
    }

    private void setupUiActions() {
        // Get transaction amount
        Bundle args = getArguments();
        if (args != null){
            binding.txnAmount.setText(args.getString("txn_amount"));
        }
        binding.btnDone.setOnClickListener(v -> requireActivity().finish());
    }

    private void observeTxnStatus() {
        dashboardViewModel.getPayFriendResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.peekContent();
            if (result != null) {
                switch (result.getStatus()) {
                    case PENDING -> onTxnPending();
                    case SUCCESS -> onTxnSuccess();
                    case ERROR -> onTxnFailed();
                }
            }
        });
    }

    private void onTxnPending() {
        binding.txnPendingAnim.setVisibility(View.VISIBLE);
        binding.txnPendingAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnPendingAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.pending));
    }

    private void onTxnSuccess() {
        binding.txnSuccessAnim.setVisibility(View.VISIBLE);
        binding.txnSuccessAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnSuccessAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.success));
    }

    private void onTxnFailed() {
        binding.txnFailedAnim.setVisibility(View.VISIBLE);
        binding.txnFailedAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnFailedAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.failed));
    }

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}