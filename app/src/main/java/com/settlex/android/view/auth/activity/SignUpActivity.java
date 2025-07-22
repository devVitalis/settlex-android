package com.settlex.android.view.auth.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.view.auth.fragment.SignupUserInfoFragment;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Load default fragment on start
        if (savedInstanceState == null) loadFragment(new SignupUserInfoFragment());
    }

    /*------------------------
    Replace Fragment Utility
    ------------------------*/
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}