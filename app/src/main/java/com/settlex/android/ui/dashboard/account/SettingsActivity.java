package com.settlex.android.ui.dashboard.account;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}