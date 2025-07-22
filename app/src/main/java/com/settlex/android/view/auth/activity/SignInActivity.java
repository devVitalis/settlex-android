package com.settlex.android.view.auth.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;
import com.settlex.android.controller.ProgressViewController;
import com.settlex.android.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize
        ProgressViewController progressViewController = new ProgressViewController(binding.fragmentContainer);

// Show when needed
        progressViewController.show();


    }
}