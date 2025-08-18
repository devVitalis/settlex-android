package com.settlex.android.data.repository;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.util.network.RequestMetadataService;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralizes all authentication operations
 */
public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseFunctions functions;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Using europe-west2 lower latency
        functions = FirebaseFunctions.getInstance("europe-west2");
    }

    /**
     * Handles email/password authentication with Firebase Auth
     * Includes specific error handling for invalid credentials
     */
    public void loginWithEmail(String email, String password, LoginCallback callback) {
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
     * Registers new user with Firebase Auth and stores profile data
     * Includes collision detection for existing accounts
     */
    public void registerUser(UserModel user, String email, String password, RegisterCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser currentUser = authResult.getUser();
                    if (currentUser == null) {
                        callback.onFailure("Authentication failed. User not found.");
                        return;
                    }
                    user.setUid(currentUser.getUid());
                    storeUserProfileAndVerify(user, callback);
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
     * Checks email availability via Cloud Function
     * Used during registration to prevent duplicate accounts
     */
    public void checkEmailExistence(String email, EmailExistenceCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        functions.getHttpsCallable("checkEmailExistence")
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
     * Manages the complete OTP verification flow:
     * 1. Sends verification code
     * 2. Validates entered code
     * 3. Handles success/failure states
     */
    public void sendEmailVerificationOtp(String email, SendOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        functions.getHttpsCallable("sendVerifyEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void verifyEmailVerificationOtp(String email, String otp, VerifyOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);
        functions.getHttpsCallable("verifyEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Handles password reset flow including:
     * - OTP generation
     * - OTP verification
     * - Secure password update
     */
    public void sendEmailPasswordResetOtp(String email, SendOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        functions.getHttpsCallable("sendPasswordResetEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void verifyEmailPasswordResetOtp(String email, String otp, VerifyOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);
        functions.getHttpsCallable("verifyPasswordResetEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Securely updates password with device metadata for fraud detection
     */
    public void resetPassword(String email, String newPassword, ChangePasswordCallback callback) {
        RequestMetadataService.collectAsync(metadata -> {
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("newPassword", newPassword);
            data.put("metadata", new Gson().toJson(metadata));

            functions.getHttpsCallable("resetPassword")
                    .call(data)
                    .addOnSuccessListener(result -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        });
    }

    /**
     * Stores user profile and initiates email verification
     * Maintains data consistency with rollback on failure
     */
    private void storeUserProfileAndVerify(UserModel user, RegisterCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", new Gson().toJson(user));

        functions.getHttpsCallable("storeUserProfile")
                .call(data)
                .addOnSuccessListener(result -> {
                    callback.onSuccess();

                    markEmailVerified(user.getUid(), new RegisterCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String reason) {
                            markEmailAsUnverified(user.getUid());
                        }
                    });
                    setUserDisplayName(user.getFirstName());
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void markEmailVerified(String uid, RegisterCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        functions.getHttpsCallable("markEmailVerified")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fallback method to maintain data consistency when verification fails
     */
    private void markEmailAsUnverified(String uid) {
        firestore.collection("users")
                .document(uid)
                .update("emailVerified", false);
    }

    /**
     * Updates user display name in Firebase Auth during registration
     * with user firstName
     */
    private void setUserDisplayName(String firstName) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder()
                    .setDisplayName(firstName)
                    .build();
            user.updateProfile(displayName);
        }
    }

    // ============================ State Management ============================
    @Nullable
    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // ===== Callback Interfaces =====
    public interface LoginCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface RegisterCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface EmailExistenceCallback {
        void onSuccess(boolean exists);
        void onFailure(String reason);
    }

    public interface SendOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface VerifyOtpCallback {
        void onSuccess();
        void onFailure(String reason);
    }

    public interface ChangePasswordCallback {
        void onSuccess();
        void onFailure(String reason);
    }
}