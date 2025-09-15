package com.settlex.android.ui.dashboard.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.ui.dashboard.fragments.transaction.PayAFriendFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Load default fragment on start
        if (savedInstanceState == null) navigateToFragment(new PayAFriendFragment());
    }

    private void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}