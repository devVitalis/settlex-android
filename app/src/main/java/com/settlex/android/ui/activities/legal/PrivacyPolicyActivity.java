package com.settlex.android.ui.activities.legal;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        setupUIActions();
    }

    /*-------------------------------
    Set up UI Event Handlers
    -------------------------------*/
    private void setupUIActions() {
        formatPrivacyPolicyText();
        setupBackButton();

        // Click listeners
        binding.imgBackBefore.setOnClickListener(v -> finish());
    }


    /*------------------------------------------------
    Load HTML formatted privacy policy into TextView
    ------------------------------------------------*/
    private void formatPrivacyPolicyText() {
        String PrivacyPolicy =
                "<b>1. Introduction</b><br>" +
                        "SettleX is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your personal information.<br><br>" +
                        "<b>2. Information We Collect</b><br>" +
                        "We may collect personal data such as your name, email, phone number, and payment information when you use SettleX.<br><br>" +
                        "<b>3. How We Use Your Information</b><br>" +
                        "We use your information to process payments, improve our services, provide customer support, and comply with legal obligations.<br><br>" +
                        "<b>4. Data Sharing</b><br>" +
                        "We do not sell your data. We may share it with trusted third parties who assist in operating our services, under strict confidentiality agreements.<br><br>" +
                        "<b>5. Security</b><br>" +
                        "We implement strong security measures to protect your data. However, no method of transmission is 100% secure.<br><br>" +
                        "<b>6. Your Choices</b><br>" +
                        "You may update your personal information or request deletion by contacting us. Certain information may be retained for legal compliance.<br><br>" +
                        "<b>7. Children's Privacy</b><br>" +
                        "SettleX is not intended for children under the age of 13. We do not knowingly collect information from minors.<br><br>" +
                        "<b>8. Changes to This Policy</b><br>" +
                        "We may update this Privacy Policy periodically. Changes will be posted in the app and take effect immediately upon posting.<br><br>" +
                        "<b>9. Contact Us</b><br>" +
                        "If you have any questions or concerns, please contact us at: <a href=\"mailto:support@settlex.com\">support@settlex.com</a>";

        binding.txtPrivacyPolicy.setText(Html.fromHtml(PrivacyPolicy, Html.FROM_HTML_MODE_LEGACY));
    }

    /*------------------------------------
    Override physical back button behavior
    -------------------------------------*/
    private void setupBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    /*--------------------------
    Customize status bar color
    --------------------------*/
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decor.setSystemUiVisibility(flags);
    }
}
