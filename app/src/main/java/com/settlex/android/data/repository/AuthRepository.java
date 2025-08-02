package com.settlex.android.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.model.UserModel;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseFunctions functions;

    /*--------------------------------------------
    Constructor to Initialize Services Instances
    --------------------------------------------*/
    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        functions = FirebaseFunctions.getInstance("europe-west2");
    }

    /*-------------------------------------------
    Create user Account with email && Password
    -------------------------------------------*/
    public void signUpWithEmailAndPassword(UserModel user, String email, String password, CreateAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();
                    user.setUid(uid);
                    saveUserProfileToDatabase(user, callback);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure("This user already exists. Kindly log in.");
                    } else {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /*------------------------------------
    Save user profile and handle callback
    -------------------------------------*/
    public void saveUserProfileToDatabase(UserModel user, CreateAccountCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", user.toMap()); // JSON-safe user data

        functions.getHttpsCallable("saveUserProfile")
                .call(data)
                .addOnSuccessListener(result -> {

                    callback.onSuccess();

                    markUserEmailVerified(user.getUid(), new CreateAccountCallback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onFailure(String reason) {
                            markEmailUnverified(user.getUid());
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*----------------------------------------
    Trigger Cloud Function To Send Email OTP
    ----------------------------------------*/
    public void sendEmailOtp(String email, SendEmailOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("sendEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*------------------------------------------
    Trigger Cloud Function To Verify Email OTP
    ------------------------------------------*/
    public void verifyEmailOtp(String email, String otp, VerifyEmailOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);

        functions.getHttpsCallable("verifyEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*--------------------------------------------
    Trigger Cloud Function To Mark Email Verified
    --------------------------------------------*/
    private void markUserEmailVerified(String uid, CreateAccountCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        functions.getHttpsCallable("markEmailVerified")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*--------------------------------------------
    Save pending flag for Unverified users (Email)
    --------------------------------------------*/
    private void markEmailUnverified(String uid) {
        db.collection("users")
                .document(uid)
                .update("emailVerifiedPending", true)
                .addOnFailureListener(e -> Log.w("EmailFlag", "Failed to set emailVerifiedPending", e));
    }

    /*--------------------------------------------
    SignIn User with Email && Password
    --------------------------------------------*/
    public void SignInWithEmailAndPassword(String email, String password, SignInCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*---------------------------------------
    Callback Interfaces For Success/Failures
    ---------------------------------------*/
    // CreateAccountCallback
    public interface CreateAccountCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    // SendEmailOtpCallback
    public interface SendEmailOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    // VerifyEmailOtpCallback
    public interface VerifyEmailOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    // SignInCallback
    public interface SignInCallback {
        void onSuccess();

        void onFailure(String reason);
    }
}