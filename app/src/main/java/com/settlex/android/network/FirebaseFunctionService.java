package com.settlex.android.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FirebaseFunctionService {

    /*------------------------------------------
    Sends POST request to sendEmailOtp function
    ------------------------------------------*/
    @POST("sendEmailOtp")
    Call<ResponseBody> sendEmailOtp(@Body EmailOtpRequest request);

    /*-------------------------------------------
    Sends POST request to verifyEmailOtp function
    -------------------------------------------*/
    @POST("verifyEmailOtp")
    Call<ResponseBody> verifyEmailOtp(@Body EmailOtpVerify verify);

}