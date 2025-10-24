package com.settlex.android.ui.dashboard.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityReceiveBinding;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceiveActivity extends AppCompatActivity {
    private ActivityReceiveBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupUiActions();
        observeUserDataStatus();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);

        binding.btnCopy.setOnClickListener(v -> copyPaymentId());
        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnShareDetails.setOnClickListener(v -> Toast.makeText(this, "This feature is not yet implemented", Toast.LENGTH_SHORT).show());
    }

    private void copyPaymentId() {
        StringUtil.copyToClipboard(this, "Payment Id", binding.paymentId.getText().toString(), true);
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(this, user -> {
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
        binding.paymentId.setText(StringUtil.addAtToPaymentId(user.getPaymentId()));
    }
}