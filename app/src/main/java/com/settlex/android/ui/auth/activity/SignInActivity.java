package com.settlex.android.ui.auth.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;
import com.settlex.android.controller.ProgressViewController;
import com.settlex.android.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private ProgressViewController loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}