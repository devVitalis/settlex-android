package com.settlex.android.ui.activities.legal;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPrivacyPolicyBinding;

/**
 * Displays HTML-formatted privacy policy with light-themed status bar.
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.txtPrivacyPolicy.setText(Html.fromHtml(getPolicyHtml(), Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
    }

    private String getPolicyHtml() {
        return "Content";
    }
}