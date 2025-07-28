package com.settlex.android.ui.auth.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.controller.ProgressViewController;
import com.settlex.android.controller.SignUpController;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.databinding.FragmentSignUpEmailVerificationBinding;
import com.settlex.android.ui.auth.viewmodel.SignUpViewModel;
import com.settlex.android.utils.string.StringUtil;

public class SignUpEmailVerificationFragment extends Fragment {

    private UserModel user;
    private SignUpViewModel vm;
    private EditText[] otpCodeInputs;
    private SignUpController controller;
    private ProgressViewController progressBar;
    private FragmentSignUpEmailVerificationBinding binding;

    /*----------------------------------
    Required Empty Public Constructor
    ----------------------------------*/
    public SignUpEmailVerificationFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpEmailVerificationBinding.inflate(getLayoutInflater(), container, false);

        controller = new SignUpController();
        progressBar = new ProgressViewController(binding.fragmentContainer);
        vm = new ViewModelProvider(requireActivity()).get(SignUpViewModel.class);
        user = vm.getUser().getValue();

        setupStatusBar();
        setupUIActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disableResendOtpBtn();
    }

    /*-----------------------------------
    Bind Views & Handle Event Listeners
    -----------------------------------*/
    private void setupUIActions() {
        formatTxtInfo();
        setupOtpCodeInputs();
        maskAndDisplayUserEmail();
        setupHideKeyboardOnTouch();

        // Click Listeners
        binding.imgViewGoBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnClearAll.setOnClickListener(view -> clearOtpInputs());
        binding.btnVerify.setOnClickListener(v -> verifyOtp());
        binding.btnResendOtp.setOnClickListener(view -> resendEmailVerificationOtp());
    }

    private void verifyOtp() {
        progressBar.show();

        String email = user.getEmail();
        String otp = getEnteredOtpCode();

        controller.verifyEmailOtp(email, otp, new SignUpController.VerifyEmailOtpCallback() {
            @Override
            public void onSuccess(String message) {
                binding.successAnim.setRenderMode(RenderMode.SOFTWARE);
                binding.successAnim.setVisibility(View.VISIBLE);
                binding.successAnim.playAnimation();

                binding.txtMessage.setText(message);
                binding.txtMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue));
                binding.txtMessage.setVisibility(View.VISIBLE);

                progressBar.hide();

                // Listen for animation end, then load next fragment
                binding.successAnim.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        loadFragment(new SignupUserInfoFragment());
                    }
                });

            }

            @Override
            public void onFailure(String reason) {
                binding.txtMessage.setText(reason);
                binding.txtMessage.setVisibility(VISIBLE);
                progressBar.hide();
            }
        });
    }

    private void resendEmailVerificationOtp() {
        progressBar.show();

        String email = user.getEmail();

        controller.sendEmailOtp(email, new SignUpController.SendEmailOtpCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                disableResendOtpBtn();
                progressBar.hide();
            }

            @Override
            public void onFailure(String reason) {
                binding.txtMessage.setText(reason);
                binding.txtMessage.setVisibility(VISIBLE);
                progressBar.hide();
            }
        });
    }


    /*------------------------------------------
    Setup passcode input chaining logic
    -------------------------------------------*/
    private void setupOtpCodeInputs() {
        otpCodeInputs = new EditText[]{
                binding.otpDigit1, binding.otpDigit2, binding.otpDigit3,
                binding.otpDigit4, binding.otpDigit5, binding.otpDigit6
        };

        for (int i = 0; i < otpCodeInputs.length; i++) {
            EditText current = otpCodeInputs[i];
            EditText next = (i < otpCodeInputs.length - 1) ? otpCodeInputs[i + 1] : null;
            EditText prev = (i > 0) ? otpCodeInputs[i - 1] : null;

            current.setLongClickable(false);
            current.setTextIsSelectable(false);
            current.setEnabled(i == 0);

            current.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpFullyEntered()) hideKeyboard();

                    binding.btnClearAll.setVisibility(TextUtils.isEmpty(s) ? GONE : VISIBLE);
                    if (TextUtils.isEmpty(s)) {
                        binding.txtMessage.setVisibility(GONE);
                    }

                    if (s.length() == 1 && next != null) {
                        next.setEnabled(true);
                        next.requestFocus();
                        next.setText("");
                    }

                    boolean isFilled = true;
                    for (EditText pin : otpCodeInputs) {
                        if (TextUtils.isEmpty(pin.getText())) {
                            isFilled = false;
                            break;
                        }
                    }
                    binding.btnVerify.setEnabled(isFilled);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(current.getText())) {
                        current.setText("");
                    } else if (prev != null) {
                        prev.setText("");
                        prev.requestFocus();
                        current.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    /*--------------------------------------------
    Check if all (OTP) input fields are filled
    --------------------------------------------*/
    private boolean isOtpFullyEntered() {
        for (EditText input : otpCodeInputs) {
            if (TextUtils.isEmpty(input.getText())) {
                return false;
            }
        }
        return true;
    }

    /*-------------------------------
    Read and join Otp field values
    -------------------------------*/
    private String getEnteredOtpCode() {
        StringBuilder pin = new StringBuilder();
        for (EditText input : otpCodeInputs) {
            pin.append(input.getText().toString().trim());
        }
        return pin.toString();
    }

    /*---------------------------------
    Reset all (OTP) fields to default
    ---------------------------------*/
    private void clearOtpInputs() {
        for (EditText input : otpCodeInputs) {
            input.setText("");
            input.setEnabled(false);
        }
        otpCodeInputs[0].setEnabled(true);
        otpCodeInputs[0].requestFocus();
    }

    /*------------------------------
    Disable Resend Btn, 60s (1min)
    ------------------------------*/
    private void disableResendOtpBtn() {
        binding.btnResendOtp.setEnabled(false);
        binding.btnResendOtp.setTag(binding.btnResendOtp.getText());

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds > 0) {
                    binding.btnResendOtp.setText("Resend in " + seconds);
                }
            }

            public void onFinish() {
                CharSequence defaultTxt = (CharSequence) binding.btnResendOtp.getTag();
                if (defaultTxt != null) binding.btnResendOtp.setText(defaultTxt);
                binding.btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    /*----------------------------------
    Get User Email (OTP) code was sent
    Mask and display in the UI
    ----------------------------------*/
    private void maskAndDisplayUserEmail() {
        if (user != null) {
            String email = user.getEmail();
            String displayEmail = "Enter the OTP sent to your email address" + "<font color='#0044CC'>" + "(" + StringUtil.maskEmail(email) + ")" + "<font/>";
            binding.txtInfoInstruction.setText(Html.fromHtml(displayEmail, Html.FROM_HTML_MODE_LEGACY));
        }
    }

    /*----------------------
    Hide (System) Keyboard
    -----------------------*/
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*---------------------------
    Format text style using HTML
    ---------------------------*/
    private void formatTxtInfo() {
        String txtInfo = "Didn’t get the email? Make sure to also " +
                "<font color='#FFA500'><b>check your spam/junk folder</b></font> " +
                "if you can't find the email in your inbox.";

        binding.txtInfo.setText(Html.fromHtml(txtInfo, Html.FROM_HTML_MODE_LEGACY));
    }

    /*----------------------------
    Navigate to another fragment
    ----------------------------*/
    private void loadFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    /*----------------------------------------------
    Dismiss keyboard and clear focus on outside tap
    ----------------------------------------------*/
    @SuppressLint("ClickableViewAccessibility")
    private void setupHideKeyboardOnTouch() {
        binding.fragmentContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                View focused = requireActivity().getCurrentFocus();
                if (focused instanceof EditText) {
                    Rect outRect = new Rect();
                    focused.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        focused.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                        }
                        binding.fragmentContainer.requestFocus();
                    }
                }
            }
            return false;
        });
    }

    /*-------------------------------
    Customize status bar appearance
    --------------------------------*/
    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
    }
}