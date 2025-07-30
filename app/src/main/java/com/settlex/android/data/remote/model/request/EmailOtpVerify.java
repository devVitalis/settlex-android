package com.settlex.android.data.remote.model.request;

public class EmailOtpVerify {

    private String email;
    private String otp;

    /*-------------------------------------------
    Constructor to initialize email && otp field
    -------------------------------------------*/
    public EmailOtpVerify(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}
