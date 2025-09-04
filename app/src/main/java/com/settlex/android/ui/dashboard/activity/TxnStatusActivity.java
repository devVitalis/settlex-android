package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.databinding.ActivityTxnStatusBinding;
import com.settlex.android.ui.dashboard.viewmodel.DashboardViewModel;
import com.settlex.android.util.event.Result;

public class TxnStatusActivity extends AppCompatActivity {
    private ActivityTxnStatusBinding binding;
    private DashboardViewModel dashboardViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTxnStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupStatusBar();


        observeTxnStatus();

        binding.btnDone.setOnClickListener(v -> finish());
    }

    private void observeTxnStatus() {
        dashboardViewModel.getPayFriendResult().observe(this, event -> {
            Result<String> result = event.peekContent();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> onTxnLoading();
                case SUCCESS -> onTxnSuccess();
                case ERROR -> onTxnFailed();
            }
        });
    }

    private void onTxnLoading() {
        binding.txnStatus.setText("Transaction Pending");
    }

    private void onTxnSuccess() {
        binding.txnSuccessLottie.setRenderMode(RenderMode.SOFTWARE);
        binding.txnSuccessLottie.playAnimation();
        binding.txnStatus.setText("Transaction Successful");
    }

    private void onTxnFailed() {
        binding.txnStatus.setText("Transaction Failed");
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}