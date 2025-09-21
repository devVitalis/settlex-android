package com.settlex.android.ui.auth.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Hosts the multi-step user registration flow using fragments.
 */
@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }
}