package com.settlex.android.data.repository;

import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.settlex.android.domain.model.UserModel;
import com.settlex.android.data.remote.api.MetadataService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

/**
 * Centralizes all authentication operations
 */
public class AuthRepository {
    private final String TAG = AuthRepository.class.getSimpleName();
    private final String ERROR_NO_INTERNET = "Connection lost. Please check your Wi-Fi or cellular data and try again";
    private final String ERROR_FALLBACK = "Something went wrong. Try again";

    private final FirebaseAuth auth;
    private final FirebaseFunctions functions;
    private final FirebaseMessaging firebaseMessaging;

    @Inject
    public AuthRepository(FirebaseAuth auth, FirebaseFunctions functions, FirebaseMessaging firebaseMessaging) {
        this.auth = auth;
        this.functions = functions;
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Sign in user using email/password with Firebase Auth
     */
    public void loginWithEmail(String email, String password, LoginCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onFailure("Incorrect email or password");
                        return;
                    }
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    if (e instanceof FirebaseAuthInvalidUserException ex) {
                        if ("ERROR_USER_DISABLED".equals(ex.getErrorCode())) {
                            callback.onFailure("Your account has been disabled, contact support");
                            return;
                        }
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Login failed: " + e.getMessage(), e);
                });
    }

    public interface LoginCallback {
        void onSuccess();

        void onFailure(String error);
    }

    /**
     * Registers new user with Firebase Auth and stores profile data
     */
    public void createAccount(UserModel user, String email, String password, CreateAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser currentUser = authResult.getUser();
                    if (currentUser == null) {
                        callback.onFailure("Authentication failed. User not found.");
                        return;
                    }
                    // get, set user unique id and store profile data
                    user.setUid(currentUser.getUid());
                    createProfileData(user, callback);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onFailure("This user already exists. Kindly log in.");
                        return;
                    }

                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Account creation failed: " + e.getMessage(), e);
                });
    }

    /**
     * Stores user profile and mark email as verify
     */
    private void createProfileData(UserModel user, CreateAccountCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", new Gson().toJson(user));

        functions.getHttpsCallable("default-createProfileData")
                .call(data)
                .addOnSuccessListener(result -> {
                    callback.onSuccess(); // return
                    setEmailVerified(user.getUid());
                    setUserDisplayName(user.getFirstName() + " " + user.getLastName());
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Profile creation failed: " + e.getMessage(), e);
                });
    }

    public interface CreateAccountCallback {
        void onSuccess();

        void onFailure(String error);
    }

    private void setEmailVerified(String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        functions.getHttpsCallable("default-setEmailVerified")
                .call(data)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark email verified: " + e.getMessage(), e));
    }

    private void setUserDisplayName(String fullName) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();
        user.updateProfile(displayName);
    }

    /**
     * Checks email availability
     */
    public void checkEmailExists(String email, CheckEmailExistsCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("default-checkEmailExists")
                .call(data)
                .addOnSuccessListener(result -> {
                    if (result.getData() != null) {
                        Map<?, ?> resultData = (Map<?, ?>) result.getData();
                        callback.onSuccess((boolean) resultData.get("exists"));
                    } else {
                        callback.onFailure("Something went wrong. Please try again");
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Check email exist failed: " + e.getMessage(), e);
                });
    }

    public interface CheckEmailExistsCallback {
        void onSuccess(boolean exists);

        void onFailure(String error);
    }


    /**
     * Sends verification code to provided email
     */
    public void sendVerificationCode(String email, SendVerificationCodeCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("default-sendVerificationCode")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Send verification code failed: " + e.getMessage(), e);
                });
    }

    /**
     * Validates entered verification
     */
    public void verifyEmail(String email, String otp, VerifyEmailCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);

        functions.getHttpsCallable("default-verifyEmail")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Email verification failed: " + e.getMessage(), e);
                });
    }

    public interface VerifyEmailCallback {
        void onSuccess();

        void onFailure(String error);
    }

    /**
     * Sends verification code during password reset
     */
    public void sendPasswordResetCode(String email, SendVerificationCodeCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("default-sendPasswordResetCode")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Send password reset code failed: " + e.getMessage(), e);
                });
    }

    public interface SendVerificationCodeCallback {
        void onSuccess();

        void onFailure(String error);
    }

    /**
     * Verify authenticity of the password rest with code
     */
    public void verifyPasswordReset(String email, String otp, VerifyPasswordCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);

        functions.getHttpsCallable("default-verifyPasswordReset")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Password reset verification failed: " + e.getMessage(), e);
                });
    }

    public interface VerifyPasswordCallback {
        void onSuccess();

        void onFailure(String error);
    }

    /**
     * Update user password
     */
    public void setNewPassword(String email, String newPassword, SetNewPasswordCallback callback) {
        MetadataService.collectMetadata(metadata -> {
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("newPassword", newPassword);
            data.put("metadata", new Gson().toJson(metadata));

            functions.getHttpsCallable("default-setNewPassword")
                    .call(data)
                    .addOnSuccessListener(result -> callback.onSuccess())
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                            callback.onFailure(ERROR_NO_INTERNET);
                            return;
                        }
                        callback.onFailure(ERROR_FALLBACK);
                        Log.e(TAG, "Password update failed: " + e.getMessage(), e);
                    });
        });
    }

    public interface SetNewPasswordCallback {
        void onSuccess();

        void onFailure(String error);
    }

    public void getFcmToken(FcmTokenCallback callback) {
        firebaseMessaging.getToken()
                .addOnSuccessListener(callback::onTokenReceived)
                .addOnFailureListener(e -> {
                    callback.onTokenError();
                    Log.e(TAG, "Failed to fetch token: " + e.getMessage(), e);
                });
    }

    public void storeNewToken(String token) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update token", e));
    }

    public interface FcmTokenCallback {
        void onTokenReceived(String token);

        void onTokenError();
    }

    // Session
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
    }
}