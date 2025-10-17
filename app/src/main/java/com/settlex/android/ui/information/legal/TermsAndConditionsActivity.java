package com.settlex.android.ui.information.legal;

import android.os.Bundle;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityTermsAndConditionsBinding;
import com.settlex.android.util.ui.StatusBarUtil;

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

        StatusBarUtil.setStatusBarColor(this, R.color.white);
        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.txtTermsAndConditions.setText(Html.fromHtml(getTermsHtml(), Html.FROM_HTML_MODE_LEGACY));
    }

    private String getTermsHtml() {
        return """
                <html>
                <body>
                    <h2>1. Acceptance of Terms</h2>
                    <p>By accessing or using the SettleX mobile application and related services (collectively, the <strong>"Service"</strong>), you agree to be bound by these Terms and Conditions (<strong>"Terms"</strong>) and our Privacy Policy. If you do not agree to these Terms, you may not use the Service. SettleX is operated by <strong>[Insert Company Name, e.g., SettleX Technologies Ltd.]</strong>, a company registered under the laws of the Federal Republic of Nigeria.</p>
                
                    <h2>2. Eligibility and Account Use</h2>
                    <ul>
                        <li><strong>2.1 Eligibility:</strong> You must be at least 18 years old and legally competent to enter into this agreement. If you are using the Service on behalf of a company or other legal entity, you represent that you have the authority to bind that entity to these Terms.</li>
                        <li><strong>2.2 Account Security:</strong> You are solely responsible for maintaining the confidentiality of your account credentials, including your password, PIN, and any biometric data used for login. You must notify SettleX immediately of any unauthorized use of your account.</li>
                        <li><strong>2.3 Accurate Information:</strong> You agree to provide accurate, current, and complete information during the registration process and to update such information to maintain its accuracy.</li>
                    </ul>
                
                    <h2>3. The SettleX Service</h2>
                    <ul>
                        <li><strong>3.1 Functionality:</strong> SettleX provides a platform for facilitating the payment of bills, purchase of airtime and data, and other digital services (<strong>"Transactions"</strong>). We rely on third-party service providers (such as banks, utility companies, and payment gateways) to complete these Transactions.</li>
                        <li><strong>3.2 Transaction Finality:</strong> All Transactions processed through SettleX are deemed final upon successful confirmation. <strong>SettleX is not responsible for any delay or failure in the delivery of services by the underlying third-party service provider</strong> (e.g., a utility company failing to reconnect service after payment).</li>
                        <li><strong>3.3 Transaction Limits:</strong> SettleX reserves the right to impose limits on the value or volume of Transactions that may be processed through your account, in line with regulatory requirements and our risk management policies.</li>
                    </ul>
                
                    <h2>4. Fees and Charges</h2>
                    <ul>
                        <li><strong>4.1 Service Fees:</strong> SettleX may charge service fees for the use of certain services, as displayed to you prior to completing a Transaction. You agree to pay all applicable fees and taxes.</li>
                        <li><strong>4.2 Changes to Fees:</strong> SettleX reserves the right to change the fee structure at any time. We will notify you of any changes by posting them on the application or website. Your continued use of the Service after the notice constitutes your acceptance of the new fee structure.</li>
                    </ul>
                
                    <h2>5. Transaction Errors and Disputes</h2>
                    <ul>
                        <li><strong>5.1 User Errors:</strong> If you input incorrect payment details (e.g., incorrect meter number, phone number, or account ID), <strong>you bear the risk of the resulting incorrect Transaction</strong>. SettleX is not obligated to reverse or refund payments made due to user error.</li>
                        <li><strong>5.2 System Errors:</strong> If a Transaction error occurs due to a technical failure within the SettleX platform, our liability is strictly limited to the amount of the incorrect Transaction. We will use commercially reasonable efforts to correct the error and credit or refund your account.</li>
                        <li><strong>5.3 Disputes:</strong> You must report any suspected unauthorized or incorrect Transactions within <strong>twenty-four (24) hours</strong> of the Transaction date. We will investigate the dispute in accordance with internal procedures and regulatory guidelines.</li>
                    </ul>
                
                    <h2>6. Prohibited Activities</h2>
                    <p>You shall not use the Service for any activities that are illegal, fraudulent, or violate any applicable laws or regulations in Nigeria, including but not limited to <strong>money laundering, funding of terrorism, or fraudulent chargebacks</strong>.</p>
                
                    <h2>7. Intellectual Property</h2>
                    <p>All content, features, and functionality of SettleX, including but not limited to, software, design, logos, and trademarks, are the <strong>exclusive property of SettleX</strong> and its licensors and are protected by intellectual property laws.</p>
                
                    <h2>8. Disclaimer of Warranties</h2>
                    <p>THE SERVICE IS PROVIDED ON AN <strong>"AS IS" AND "AS AVAILABLE"</strong> BASIS. SETTLEX EXPRESSLY DISCLAIMS ALL WARRANTIES OF ANY KIND, WHETHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.</p>
                
                    <h2>9. Limitation of Liability</h2>
                    <p>TO THE <strong>MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW</strong>, IN NO EVENT SHALL SETTLEX, ITS AFFILIATES, DIRECTORS, OR EMPLOYEES BE LIABLE FOR ANY INDIRECT, PUNITIVE, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR EXEMPLARY DAMAGES, INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF PROFITS, GOODWILL, USE, DATA, OR OTHER INTANGIBLE LOSSES, ARISING OUT OF OR RELATING TO THE USE OF, OR INABILITY TO USE, THE SERVICE.</p>
                
                    <h2>10. Governing Law and Dispute Resolution</h2>
                    <ul>
                        <li><strong>10.1 Governing Law:</strong> These Terms shall be governed by and construed in accordance with the laws of the <strong>Federal Republic of Nigeria</strong>, without regard to its conflict of law principles.</li>
                        <li><strong>10.2 Dispute Resolution:</strong> Any dispute, controversy, or claim arising out of or relating to these Terms, or the breach thereof, shall be first addressed through good faith negotiation. If the dispute cannot be resolved within <strong>thirty (30) days</strong>, the parties agree to submit the dispute to the appropriate courts in <strong>Lagos, Nigeria</strong>.</li>
                    </ul>
                
                    <h2>11. Changes to the Terms</h2>
                    <p>We reserve the right, at our sole discretion, to modify or replace these Terms at any time. We will provide notice of material changes at least <strong>30 days</strong> prior to the effective date. By continuing to access or use our Service after those revisions become effective, you agree to be bound by the revised terms.</p>
                </body>
                </html>
                """;
    }
}