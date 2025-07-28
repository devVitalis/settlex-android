package com.settlex.android.controller;

import com.settlex.android.manager.AuthManager;

public class SignUpController {

    private AuthManager auth;

    // Initialized instances in the constructor
    public SignUpController() {
        auth = new AuthManager();
    }

    /*------------------------------------------------------------------------
    Trigger OTP email via AuthManager and relay success/failure via callback
    ------------------------------------------------------------------------*/
    public void sendEmailOtp(String email, SendEmailOtpCallback callback) {
        auth.sendEmailOtp(email, new AuthManager.SendEmailOtpCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess("OTP Sent!");
            }

            @Override
            public void onFailure(String reason) {
                callback.onFailure(reason);
            }
        });
    }

    public void verifyEmailOtp(String email, String otp, VerifyEmailOtpCallback cb){
        auth.verifyEmailOtp(email, otp, new AuthManager.VerifyEmailOtpCallback() {
            @Override
            public void onSuccess() {
                cb.onSuccess("Verification Successful");
            }

            @Override
            public void onFailure(String reason) {
                cb.onFailure(reason);
            }
        });
    }

    /*---------------------------------------
    Callback Interfaces For Success/Failures
    ---------------------------------------*/
    public interface SendEmailOtpCallback {
        void onSuccess(String message);

        void onFailure(String reason);
    }

    public interface VerifyEmailOtpCallback {
        void onSuccess(String message);

        void onFailure(String reason);
    }
}
