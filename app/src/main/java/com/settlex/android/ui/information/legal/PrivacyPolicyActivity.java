package com.settlex.android.ui.information.legal;

import android.os.Bundle;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPrivacyPolicyBinding;
import com.settlex.android.util.ui.StatusBarUtil;

/**
 * Displays HTML-formatted privacy policy with light-themed status bar.
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StatusBarUtil.setStatusBarColor(this, R.color.white);
        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.txtPrivacyPolicy.setText(Html.fromHtml(getPolicyHtml(), Html.FROM_HTML_MODE_LEGACY));
    }

    private String getPolicyHtml() {
        return """
                <html>
                <body>
                    <h1>SettleX - Privacy Policy</h1>
                    <p><strong>Last Updated: October 2025</strong></p>
                
                    <h2>1. Introduction</h2>
                    <p>This Privacy Policy describes how SettleX ("we," "us," or "our") collects, uses, processes, and shares your information when you use the SettleX mobile application and our services. We are committed to protecting your personal data in compliance with the Nigerian Data Protection Regulation (NDPR) and other applicable Nigerian laws.</p>
                
                    <h2>2. Information We Collect</h2>
                    <p>We collect information necessary to provide, maintain, and improve our Service. This information falls into three categories:</p>
                
                    <h3>2.1 Personal Identification Information:</h3>
                    <ul>
                        <li><strong>Contact Details:</strong> Name, email address, phone number.</li>
                        <li><strong>Identity Verification:</strong> Government-issued ID details (e.g., if legally required), Bank Verification Number (BVN), and date of birth, solely for compliance and verification purposes.</li>
                        <li><strong>Security Data:</strong> Encrypted passwords, PINs, or biometric data used to secure your account.</li>
                    </ul>
                
                    <h3>2.2 Transaction and Financial Information:</h3>
                    <ul>
                        <li><strong>Funding Details (Bank Transfer):</strong> Information related to funds transferred into your SettleX account, which may include the originating bank account name and bank (provided by our payment processor). Since funding is done via bank transfer, <strong>we do not collect or store your payment card numbers or card tokens.</strong></li>
                        <li><strong>Transaction History:</strong> Details of bills paid, airtime purchased, amount, date, time, and associated service provider identifiers (e.g., meter numbers, cable TV account IDs).</li>
                    </ul>
                
                    <h3>2.3 Usage and Device Information:</h3>
                    <ul>
                        <li><strong>Technical Data:</strong> IP address, device type, operating system, unique device identifiers, and mobile network information.</li>
                        <li><strong>Location Data:</strong> General geographical location inferred from your IP address or device settings (if permissions are granted).</li>
                        <li><strong>Application Data:</strong> Information about how you use SettleX, such as the pages you view and the features you access.</li>
                    </ul>
                
                    <h2>3. How We Use Your Information</h2>
                    <p>We use the information we collect for the following primary purposes:</p>
                    <ul>
                        <li><strong>Service Delivery:</strong> To process and complete your Transactions (bill payments, airtime purchases).</li>
                        <li><strong>Account Management:</strong> To create, maintain, and secure your account, and to verify your identity.</li>
                        <li><strong>Communication:</strong> To send you Transaction confirmations, receipts, security alerts, and technical notices.</li>
                        <li><strong>Compliance and Security:</strong> To comply with legal and regulatory obligations, prevent fraud, and enforce our Terms and Conditions.</li>
                        <li><strong>Service Improvement:</strong> To analyze usage patterns, diagnose technical issues, and improve the functionality and speed of SettleX.</li>
                        <li><strong>Marketing (with consent):</strong> To send you promotional materials about SettleX services, where you have provided consent and an opt-out mechanism is available.</li>
                    </ul>
                
                    <h2>4. Sharing Your Information</h2>
                    <p>We do not sell your personal data. We only share your information with trusted third parties in the following circumstances:</p>
                    <ul>
                        <li><strong>Service Providers:</strong> We share necessary transaction data with third-party service providers, such as payment processors, banks, and the utility/service companies (e.g., PHCN, DSTV) to execute your requested Transaction.</li>
                        <li><strong>Legal Requirements:</strong> When required by law, court order, or governmental regulation, or if we believe disclosure is necessary to prevent physical harm or financial loss.</li>
                        <li><strong>Business Transfers:</strong> In connection with a merger, acquisition, or sale of assets, provided the receiving party agrees to adhere to the terms of this Privacy Policy.</li>
                    </ul>
                
                    <h2>5. Data Security</h2>
                    <p>We implement robust administrative, technical, and physical security measures, including data encryption, firewalls, and secure socket layer (SSL) technology, to protect your personal data against accidental or unlawful destruction, loss, alteration, unauthorized disclosure, or access. <strong>However, no internet transmission is 100% secure, and we cannot guarantee absolute security.</strong></p>
                
                    <h2>6. Data Retention</h2>
                    <p>We retain your personal information only for as long as necessary to fulfill the purposes outlined in this Policy, comply with our legal obligations (such as anti-money laundering regulations), resolve disputes, and enforce our agreements.</p>
                
                    <h2>7. Your Privacy Rights</h2>
                    <p>Subject to applicable law, you have the right to:</p>
                    <ul>
                        <li><strong>Access:</strong> Request a copy of the personal data we hold about you.</li>
                        <li><strong>Correction:</strong> Request correction of any inaccurate or incomplete data we hold.</li>
                        <li><strong>Withdraw Consent:</strong> Withdraw your consent for processing where consent is the legal basis for processing your data.</li>
                        <li><strong>Deletion (Right to be Forgotten):</strong> Request the deletion of your personal data, subject to our legal and regulatory obligations.</li>
                    </ul>
                
                    <h2>8. Contact Us</h2>
                    <p>If you have questions about this Privacy Policy or our data practices, please contact our Data Protection Officer at:</p>
                    <ul>
                        <li><strong>Email:</strong> privacy@settlex.ng</li>
                        <li><strong>Address:</strong> [Insert SettleX Corporate Address in Nigeria]</li>
                    </ul>
                </body>
                </html>
                """;
    }
}