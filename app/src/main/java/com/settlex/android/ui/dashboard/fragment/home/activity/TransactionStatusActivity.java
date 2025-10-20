package com.settlex.android.ui.dashboard.fragment.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.databinding.ActivityTransactionStatusBinding;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.util.ui.StatusBarUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * This screen displays the outcome of a transaction (Pending, Success, Failed).
 */
@AndroidEntryPoint
public class TransactionStatusActivity extends AppCompatActivity {
    private static final String TAG = TransactionStatus.class.getSimpleName();
    private ActivityTransactionStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUiActions();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);

        showTransactionData();

        binding.btnDone.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finishAffinity();
        });
    }

    private void showTransactionData() {
        String txnAmount = getIntent().getStringExtra("amount");
        String txnStatusString = getIntent().getStringExtra("status");

        if (txnAmount == null || txnStatusString == null) {
            Log.e(TAG, "Missing transaction data: amount or status is null");
            return;
        }

        // Display amount
        binding.txnAmount.setText(txnAmount);

        // Convert back to enum
        TransactionStatus txnStatus = null;
        try {
            txnStatus = TransactionStatus.valueOf(txnStatusString);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid transaction status: " + txnStatusString, e);
        }

        // Handle UI state based on status
        switch (Objects.requireNonNull(txnStatus)) {
            case SUCCESS -> showSuccessState();
            case PENDING -> showPendingState();
            case FAILED -> showFailedState();
        }
    }

    private void showPendingState() {
        String PENDING_STATUS = "Transaction Pending";
        binding.txnPendingAnim.setVisibility(View.VISIBLE);
        binding.txnPendingAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnPendingAnim.playAnimation();
        binding.txnStatus.setText(PENDING_STATUS);
    }

    private void showSuccessState() {
        String SUCCESS_STATUS = "Transaction Successful";
        binding.txnSuccessAnim.setVisibility(View.VISIBLE);
        binding.txnSuccessAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnSuccessAnim.playAnimation();
        binding.txnStatus.setText(SUCCESS_STATUS);
    }

    private void showFailedState() {
        String FAILED_STATUS = "Transaction Failed";
        binding.txnFailedAnim.setVisibility(View.VISIBLE);
        binding.txnFailedAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnFailedAnim.playAnimation();
        binding.txnStatus.setText(FAILED_STATUS);
    }
}