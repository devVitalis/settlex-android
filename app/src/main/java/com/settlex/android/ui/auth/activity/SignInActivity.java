package com.settlex.android.ui.auth.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

    private final boolean[] isPasswordVisible = {false};
    private Drawable icVisibilityOn;
    private Drawable icVisibilityLock;


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
        setupPasswordToggle();
        setupEditTxtInputFocusHandler();

        // Handle Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> loadActivity(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> loadActivity(AuthHelpActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptSignIn());
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
                binding.signInError.setVisibility(View.VISIBLE);
            }
            progressBar.hide();
        });
    }

    /*-------------------------------------------
    Enable Dynamic Stroke Color on editText bg
    -------------------------------------------*/
    private void setupEditTxtInputFocusHandler() {
        binding.editTxtEmail.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.emailInputBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_focused);
            } else {
                binding.emailInputBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_not_focused);
            }
        });
        binding.editTxtPassword.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.passwordInputBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_focused);
            } else {
                binding.passwordInputBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_not_focused);
            }
        });
    }

    /*-------------------------------------------------
    Enable login button only when both fields filled
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

                binding.signInError.setVisibility(View.GONE);
            }
        };
        binding.editTxtEmail.addTextChangedListener(watcher);
        binding.editTxtPassword.addTextChangedListener(watcher);
    }

    /*------------------------------------
    Setup Method for Password Toggle
    -------------------------------------*/
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle() {
        icVisibilityOn = ContextCompat.getDrawable(this, R.drawable.ic_visibility_on);
        icVisibilityLock = ContextCompat.getDrawable(this, R.drawable.ic_visibility_lock);

        EditText editTxtPassword = binding.editTxtPassword;

        editTxtPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // Listen for text changes
        editTxtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean showToggle = s.length() > 0;
                updatePasswordToggle(showToggle);
            }
        });

        // Handle toggle icon click
        editTxtPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = editTxtPassword.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    int touchStart = editTxtPassword.getWidth()
                            - editTxtPassword.getPaddingEnd()
                            - drawableWidth;
                    if (event.getX() >= touchStart) {
                        togglePasswordVisibility(editTxtPassword);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /*------------------------------------
    Toggle Password Visibility
    -------------------------------------*/
    private void togglePasswordVisibility(EditText editText) {
        if (isPasswordVisible[0]) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icVisibilityLock, null);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icVisibilityOn, null);
        }
        isPasswordVisible[0] = !isPasswordVisible[0];
        editText.setSelection(editText.getText().length());
    }

    /*------------------------------------
    Show or Hide Toggle Icon
    -------------------------------------*/
    private void updatePasswordToggle(boolean showToggle) {
        EditText editText = binding.editTxtPassword;
        if (showToggle) {
            if (isPasswordVisible[0]) {
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icVisibilityOn, null);
            } else {
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icVisibilityLock, null);
            }
        } else {
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
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