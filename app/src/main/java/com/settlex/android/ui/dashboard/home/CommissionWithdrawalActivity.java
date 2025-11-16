package com.settlex.android.ui.dashboard.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityCommissionWithdrawalBinding;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommissionWithdrawalActivity extends AppCompatActivity {
    private ActivityCommissionWithdrawalBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommissionWithdrawalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        observeUserData();
        setupUiActions();
    }

    private void setupUiActions() {
        StatusBar.setStatusBarColor(this, R.color.white);

        binding.btnWithdraw.setOnClickListener(v -> Toast.makeText(this, "This feature is not yet implemented", Toast.LENGTH_SHORT).show());
        binding.btnBackBefore.setOnClickListener(v -> finish());
    }

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) return;

            switch (user.status) {
                case SUCCESS -> onUserDataSuccess(user.data);
                case FAILURE -> {
                    // TODO: handle error
                }
            }
        });
    }

    private void onUserDataSuccess(UserUiModel user) {
        binding.userCommissionBalance.setText(CurrencyFormatter.formatToNaira(user.getCommissionBalance()));
    }
}