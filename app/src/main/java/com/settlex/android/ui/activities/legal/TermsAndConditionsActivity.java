package com.settlex.android.ui.activities.legal;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.settlex.android.R;
import com.settlex.android.databinding.ActivityTermsAndConditionsBinding;

/**
 * Displays the app's Terms & Conditions with HTML formatting.
 * Maintains consistent UI behavior with PrivacyPolicyActivity.
 */
public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTermsAndConditionsBinding binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.txtTermsAndConditions.setText(Html.fromHtml(getTermsHtml(), Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
    }

    private String getTermsHtml() {
        return "Content";
    }
}