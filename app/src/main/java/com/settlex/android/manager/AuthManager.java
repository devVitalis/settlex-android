package com.settlex.android.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.network.EmailOtpRequest;
import com.settlex.android.network.EmailOtpVerify;
import com.settlex.android.network.FirebaseFunctionService;
import com.settlex.android.network.RetrofitClient;
import com.settlex.android.utils.network.ResponseHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthManager {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseFunctions fun;
    private FirebaseFunctionService service;

    // Initialize Firebase Instances in the constructor
    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fun = FirebaseFunctions.getInstance("europe-west2");
        service = RetrofitClient.getService();
    }

    /*-----------------------------------------------------------------
    Create user account in FirebaseAuth using email/password.
    Called during onboarding before storing user profile in Firestore.
    -----------------------------------------------------------------*/
    public void createUserAccount(UserModel user, String email, String password, CreateUserAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Get and set user unique UID
                    String uid = Objects.requireNonNull(authResult.getUser()).getUid();
                    user.setUid(uid);
                    saveUserToDatabase(user, callback);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure("This email is already registered. Kindly log in");
                    } else {
                        callback.onFailure("Registration failed, try again!");
                    }
                });
    }

    /*----------------------------------
    Create user profile in Firestore db
    ----------------------------------*/
    private void saveUserToDatabase(UserModel user, CreateUserAccountCallback callback) {
        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Failed to save user profile"));
    }

    /*-------------------------------------------
    Mark User email verified (Callable Function)
    -------------------------------------------*/
    private void markEmailVerified(CreateUserAccountCallback callback) {
        fun.getHttpsCallable("markEmailVerified")
                .call(null)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*----------------------------------------------
    Callable function to send OTP via (Server Side)
    ----------------------------------------------*/
    public void sendEmailOtp(String email, SendEmailOtpCallback callback) {
        EmailOtpRequest request = new EmailOtpRequest(email);
        service.sendEmailOtp(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String reason = ResponseHandler.getErrorMessage(response);
                    callback.onFailure(reason);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    /*---------------------------------------------
    Callable function to verify OTP (Server Side)
    ---------------------------------------------*/
    public void verifyEmailOtp(String email, String otp, VerifyEmailOtpCallback callback) {
        EmailOtpVerify verify = new EmailOtpVerify(email, otp);
        service.verifyEmailOtp(verify).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    callback.onSuccess();
                } else {
                    String reason = ResponseHandler.getErrorMessage(response);
                    callback.onFailure(reason);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    /*---------------------------------------
    Callback Interfaces For Success/Failures
    ---------------------------------------*/
    public interface CreateUserAccountCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    public interface SendEmailOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    public interface VerifyEmailOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }
}