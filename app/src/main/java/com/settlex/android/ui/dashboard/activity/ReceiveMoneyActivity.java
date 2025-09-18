package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityReceiveMoneyBinding;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceiveMoneyActivity extends AppCompatActivity {
    private ActivityReceiveMoneyBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiveMoneyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeUserData();
        setupUiActions();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);

        binding.btnCopy.setOnClickListener(v -> copyAccountNumber());
        binding.btnBackBefore.setOnClickListener(v -> finish());
    }

    private void copyAccountNumber() {
        StringUtil.copyToClipboard(this, "Account Number", binding.accountNumber.getText().toString());
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

        if (hasUsername) {
            binding.accountNumber.setText(StringUtil.addAtToUsername(user.getUsername()));
            return;
        }
        // do something TODO: prompt username creation
    }
}