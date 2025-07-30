package com.settlex.android.data.remote.api;

import com.settlex.android.data.remote.model.request.EmailOtpRequest;
import com.settlex.android.data.remote.model.request.EmailOtpVerify;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FunctionsService {

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