package com.settlex.android.ui.auth.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySignInBinding;
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.DashboardActivity;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    private AuthViewModel vm;
    private ActivitySignInBinding binding;
    private SettleXProgressBarController progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBar = new SettleXProgressBarController(binding.fragmentContainer);

        setupStatusBar();
        setupUIActions();
    }

    /*---------------------------
    Setup UI and Event Handlers
    ---------------------------*/
    private void setupUIActions() {
        reEnableFocus();
        formatSignUpText();
        setupEditTxtInputWatcher();
        setupEditTxtEmailFocusHandler();

        // Hide end icon on default
        binding.txtInputLayoutPassword.setEndIconVisible(false);

        // Handle Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> loadActivity(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> loadActivity(AuthHelpActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptSignIn());
    }


    /*-----------------------------------
    Sign in with Email && Password ()
    -----------------------------------*/
    private void attemptSignIn() {
        progressBar.show();

        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        vm.signInUser(email, password);

        vm.getSignInResult().observe(this, signInResult -> {
            if (signInResult.isSuccess()) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, signInResult.message(), Toast.LENGTH_LONG).show();
            }
            progressBar.hide();
        });
    }

    /*--------------------------------
    Enable focus on tap for EditTexts
    ---------------------------------*/
    private void reEnableFocus() {
        View.OnClickListener enableFocusListener = v -> {
            if (v instanceof EditText editText) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        };
        binding.editTxtEmail.setOnClickListener(enableFocusListener);
        binding.editTxtPassword.setOnClickListener(enableFocusListener);
    }

    /*-------------------------------------------
    Enable Dynamic Stroke Color on editText bg
    -------------------------------------------*/
    private void setupEditTxtEmailFocusHandler() {
        binding.editTxtEmail.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.editTxtEmailBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_focused);
            } else {
                binding.editTxtEmailBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_not_focused);
            }
        });
    }

    /*-------------------------------------------------
    Enable login button only when both fields filled
    * && Handle Password End Icon Visibility
    -------------------------------------------------*/
    private void setupEditTxtInputWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
                String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

                binding.btnSignIn.setEnabled(!email.isEmpty() && !password.isEmpty());
            }
        };
        binding.editTxtEmail.addTextChangedListener(watcher);
        binding.editTxtPassword.addTextChangedListener(watcher);

        // End Icon Visibility
        binding.editTxtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isEmpty = TextUtils.isEmpty(charSequence);
                binding.txtInputLayoutPassword.setEndIconVisible(!isEmpty);
            }
        });
    }

    /*----------------------------
    Launch activity from context
    -----------------------------*/
    private void loadActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    /*------------------------------
    Style the "Sign Up" text color
    -------------------------------*/
    private void formatSignUpText() {
        String signUpText = "Don't have an account yet? <font color='#0044CC'><br>Click here to register</font>";
        binding.btnSignUp.setText(Html.fromHtml(signUpText, Html.FROM_HTML_MODE_LEGACY));
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

                    binding.fragmentContainer.requestFocus();
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