package com.settlex.android.ui.auth.components;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityOtpVerificationBinding;

public class OtpVerificationActivity extends AppCompatActivity {
    private ActivityOtpVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        formatMessageTxt();
        formatTxtInfo();

    }

    /*------------------------------
    Format texts style using HTML
    ------------------------------*/
    private void formatMessageTxt() {
        String htmlText = "Please check the OTP that<br>" +
                "has been sent to your email account<br>" +
                "<font color='#0000FF'>be****k@gmail.com</font>";
        binding.txtUserEmail.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void formatTxtInfo() {
        String txtInfo = "Didn’t get the email? Make sure to also " +
                "<font color='#FFA500'><b>check your spam/junk folder</b></font> " +
                "if you can't find the email in your inbox.";

        binding.txtInfo.setText(Html.fromHtml(txtInfo, Html.FROM_HTML_MODE_LEGACY));
    }

    /*---------------------------------------------
       Dismiss keyboard and clear focus on outside tap
       ----------------------------------------------*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            if (currentFocus instanceof EditText) {
                Rect outRect = new Rect();
                currentFocus.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    currentFocus.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }

                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /*-------------------------------------
    Set up status bar appearance and flags
    -------------------------------------*/
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decor.setSystemUiVisibility(flags);
    }
}