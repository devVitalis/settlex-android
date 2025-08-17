package com.settlex.android.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.util.network.RequestMetadataService;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository layer handling authentication-related operations.
 * Centralizes interactions with Firebase Authentication, Firestore, and Cloud Functions.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseFunctions functions;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Cloud Functions are deployed to 'europe-west2' for data residency and latency optimization.
        functions = FirebaseFunctions.getInstance("europe-west2");
    }

    /**
     * Authenticates a user with email and password.
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
     * Registers a new user with Firebase and then stores their profile in Firestore.
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
     * Checks if an email is already registered by calling a Cloud Function.
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
     * Sends a one-time password (OTP) for email verification.
     */
    public void sendEmailVerificationOtp(String email, SendOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        functions.getHttpsCallable("sendVerifyEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Verifies the OTP entered for email verification.
     */
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
     * Sends an OTP for password reset.
     */
    public void sendEmailPasswordResetOtp(String email, SendOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        functions.getHttpsCallable("sendPasswordResetEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Verifies the OTP for password reset.
     */
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
     * Changes user password via backend API with security metadata.
     */
    public void resetPassword(String email, String newPassword, ChangePasswordCallback callback) {
        RequestMetadataService.collectAsync(metadata -> {
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("newPassword", newPassword);
            data.put("metadata", new Gson().toJson(metadata)); // (Convert to JSON safe)

            functions.getHttpsCallable("resetPassword")
                    .call(data)
                    .addOnSuccessListener(result -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        });
    }

    /**
     * Saves user profile in backend and marks email as verified.
     */
    private void storeUserProfileAndVerify(UserModel user, RegisterCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", new Gson().toJson(user));
        functions.getHttpsCallable("storeUserProfile")
                .call(data)
                .addOnSuccessListener(result -> {
                    // Save the user profile, then attempt to mark the email as verified.
                    callback.onSuccess();
                    markEmailVerified(user.getUid(), new RegisterCallback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(String reason) {
                            // If failed, flag the profile to maintain a consistent state.
                            markEmailAsUnverified(user.getUid());
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Marks the user's email as verified in the backend.
     */
    private void markEmailVerified(String uid, RegisterCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        functions.getHttpsCallable("markEmailVerified")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Flags a user's email as unverified in Firestore if the verification process fails.
     */
    private void markEmailAsUnverified(String uid) {
        firestore.collection("users")
                .document(uid)
                .update("emailVerified", false);
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