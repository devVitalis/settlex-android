package com.settlex.android.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.model.UserModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository layer handling authentication-related operations.
 * Centralizes Firebase Auth, Firestore, and Cloud Functions interactions.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseFunctions cloudFunctions;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        cloudFunctions = FirebaseFunctions.getInstance("europe-west2");
    }

    /**
     * Authenticates a user with email and password.
     */
    public void loginWithEmail(String email, String password, LoginWithEmailCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onFailure("Invalid email or password");
                    } else {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Registers a new user and stores their profile in Firestore.
     */
    public void registerUser(UserModel user, String email, String password, RegisterUserCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser currentUser = authResult.getUser();
                    if (currentUser == null) {
                        callback.onFailure("Authentication failed. User not found.");
                        return;
                    }
                    user.setUid(currentUser.getUid());
                    storeUserProfile(user, callback);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure("This user already exists. Kindly log in.");
                    } else {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Checks if an email is already registered by calling a Cloud Function.
     */
    public void checkEmailExistence(String email, CheckEmailExistenceCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        cloudFunctions.getHttpsCallable("checkEmailExists")
                .call(data)
                .addOnSuccessListener(result -> {
                    if (result.getData() != null) {
                        boolean exists = Boolean.TRUE.equals(((Map<?, ?>) result.getData()).get("exists"));
                        callback.onSuccess(exists);
                    } else {
                        callback.onFailure("Invalid response from server.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Sends an OTP for email verification.
     */
    public void sendEmailVerificationOtp(String email, SendEmailVerificationOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        cloudFunctions.getHttpsCallable("sendVerifyEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Verifies the OTP entered for email verification.
     */
    public void verifyEmailVerificationOtp(String email, String otp, VerifyEmailVerificationOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);
        cloudFunctions.getHttpsCallable("verifyEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Sends an OTP for password reset.
     */
    public void sendPasswordResetOtp(String email, SendPasswordResetOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        cloudFunctions.getHttpsCallable("sendPasswordResetEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Verifies the OTP for password reset.
     */
    public void verifyPasswordResetOtp(String email, String otp, VerifyPasswordResetOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);
        cloudFunctions.getHttpsCallable("verifyPasswordResetEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Saves user profile in backend via Cloud Function and marks email as verified.
     */
    private void storeUserProfile(UserModel user, RegisterUserCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", user.toMap());
        cloudFunctions.getHttpsCallable("saveUserProfile")
                .call(data)
                .addOnSuccessListener(result -> {
                    callback.onSuccess();
                    markEmailVerified(user.getUid(), new RegisterUserCallback() {
                        @Override
                        public void onSuccess() { }

                        @Override
                        public void onFailure(String reason) {
                            setEmailUnverified(user.getUid());
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Marks the user's email as verified in backend.
     */
    private void markEmailVerified(String uid, RegisterUserCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        cloudFunctions.getHttpsCallable("markEmailVerified")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Flags user email as unverified in Firestore if verification process fails.
     */
    private void setEmailUnverified(String uid) {
        firestore.collection("users")
                .document(uid)
                .update("emailVerifiedPending", true)
                .addOnFailureListener(e -> Log.w("EmailFlag", "Failed to set emailVerifiedPending", e));
    }

    // ===== Callback Interfaces =====
    public interface LoginWithEmailCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface RegisterUserCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface CheckEmailExistenceCallback {
        void onSuccess(boolean exists);
        void onFailure(String reason);
    }

    public interface SendEmailVerificationOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface VerifyEmailVerificationOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface SendPasswordResetOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface VerifyPasswordResetOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }
}