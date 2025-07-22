package com.settlex.android.view.activities;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityTermsAndConditionsBinding;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private ActivityTermsAndConditionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        setUpUIActions();
    }

    /*-----------------------------------------
    Set up UI click listeners and interactions
    -----------------------------------------*/
    private void setUpUIActions() {
        setupBackButton();
        formatTermsAndCondition();

        // Click listeners
        binding.imgBackBefore.setOnClickListener(v -> finish());
    }

    /*-----------------------------------------------
    Load HTML formatted Terms & Conditions content
    -----------------------------------------------*/
    private void formatTermsAndCondition() {
        String TermsAndConditions =
                "<b>1. Acceptance of Terms</b><br>" +
                        "By accessing or using SettleX, you confirm that you are at least 18 years old and agree to these Terms & Conditions.<br><br>" +
                        "<b>2. Services</b><br>" +
                        "SettleX enables users to pay various utility bills and related services via a secure and user-friendly mobile platform.<br><br>" +
                        "<b>3. User Responsibilities</b><br>" +
                        "- Provide accurate and complete account and payment information<br>" +
                        "- Maintain confidentiality of your login credentials<br>" +
                        "- Use the app only for lawful purposes<br><br>" +
                        "<b>4. Payments</b><br>" +
                        "All transactions are processed securely. SettleX is not liable for incorrect payment details entered by the user.<br><br>" +
                        "<b>5. Fees</b><br>" +
                        "Some transactions may include service charges. All fees will be clearly displayed before payment is confirmed.<br><br>" +
                        "<b>6. Account Termination</b><br>" +
                        "We reserve the right to suspend or terminate your access to SettleX for violating these terms or any applicable laws.<br><br>" +
                        "<b>7. Limitation of Liability</b><br>" +
                        "SettleX is not liable for indirect, incidental, or consequential damages arising from your use of the app.<br><br>" +
                        "<b>8. Changes to Terms</b><br>" +
                        "We may update these Terms & Conditions from time to time. Updates will be posted in the app and take effect immediately.<br><br>" +
                        "<b>9. Contact Us</b><br>" +
                        "For questions or concerns, contact us at: <a href=\"mailto:support@settlex.com\">support@settlex.com</a>";

        binding.txtTermsAndConditions.setText(Html.fromHtml(TermsAndConditions, Html.FROM_HTML_MODE_LEGACY));
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
