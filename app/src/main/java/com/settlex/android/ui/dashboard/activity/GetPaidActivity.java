package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityGetPaidBinding;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GetPaidActivity extends AppCompatActivity {
    private ActivityGetPaidBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGetPaidBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeUserData();
        setupUiActions();
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

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataSuccess(user.getData());
                case ERROR -> {
                    // TODO: handle error
                }
            }
        });
    }

    private void onUserDataSuccess(UserUiModel user) {
        boolean hasUsername = user.getUsername() != null && !user.getUsername().isEmpty();
        binding.paymentName.setText(user.getFullName().toUpperCase());

        if (hasUsername) {
            binding.paymentId.setText(user.getUsername());
            return;
        }
        // do something TODO: prompt username creation
    }
}