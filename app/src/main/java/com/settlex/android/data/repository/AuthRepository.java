package com.settlex.android.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.model.UserModel;
import com.settlex.android.data.remote.model.request.EmailOtpRequest;
import com.settlex.android.data.remote.model.request.EmailOtpVerify;
import com.settlex.android.data.remote.api.FunctionsService;
import com.settlex.android.data.remote.RetrofitClient;
import com.settlex.android.utils.ResponseHandler;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    /*------------------------------------
    Initialize Firebase Auth, Firestore,
    and Retrofit Service Instance
    -------------------------------------*/
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseFunctions functions;
    private FunctionsService service;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        service = RetrofitClient.getService();
        functions = FirebaseFunctions.getInstance("us-central1");
    }

    /*-------------------------------------------
    Create user Account with email && Password
    -------------------------------------------*/
    public void createUserAccount(UserModel user, String email, String password, CreateAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();
                            user.setUid(uid);

                            saveUserProfileToDatabase(user, callback);
                        }
                    } else {
                        Exception e = task.getException();
                        String reason = (e != null) ? e.getMessage() : "Error occurred";
                        callback.onFailure(reason);
                    }
                });
    }

    /*-------------------------------------------
    Callable Function To Save User Profile in Db
    -------------------------------------------*/
    public void saveUserProfileToDatabase(UserModel user, CreateAccountCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", user.toMap()); // JSON-safe user data

        functions.getHttpsCallable("saveUserProfile")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*----------------------------------------
    Trigger Cloud Function To Send Email OTP
    ----------------------------------------*/
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

    /*------------------------------------------
    Trigger Cloud Function To Verify Email OTP
    ------------------------------------------*/
    public void verifyEmailOtp(String email, String otp, VerifyEmailOtpCallback callback) {
        EmailOtpVerify verify = new EmailOtpVerify(email, otp);
        service.verifyEmailOtp(verify).enqueue(new Callback<>() {
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

    /*--------------------------------------------
    Trigger Cloud Function To Mark Email Verified
    --------------------------------------------*/
    private void markUserEmailVerified() {

    }

    /*---------------------------------------
    Callback Interfaces For Success/Failures
    ---------------------------------------*/
    public interface CreateAccountCallback {
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