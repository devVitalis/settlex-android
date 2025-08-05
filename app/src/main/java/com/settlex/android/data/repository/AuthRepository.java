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

    /*--------------------------------------------
    SignIn User with Email && Password
    --------------------------------------------*/
    public void signInWithEmail(String email, String password, SignInCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onFailure("Invalid email or password");
                    } else {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    /*-------------------------------------------
    Create user Account with email && Password
    -------------------------------------------*/
    public void signUpWithEmailAndPassword(UserModel user, String email, String password, CreateAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser currentUser = authResult.getUser();
                    if (currentUser == null) {
                        callback.onFailure("Authentication failed. User not found.");
                        return;
                    }
                    String uid = currentUser.getUid();
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
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(String reason) {
                            markEmailUnverified(user.getUid());
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*----------------------------------------------------
    Check if email (user) already exist in Firebase Auth
    ----------------------------------------------------*/
    public void checkEmailExist(String email, CheckEmailExistCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("checkEmailExists")
                .call(data)
                .addOnSuccessListener(result -> {
                    if (result.getData() != null) {
                        boolean exist = Boolean.TRUE.equals(((Map<?, ?>) result.getData()).get("exists"));
                        callback.onSuccess(exist);
                    } else {
                        callback.onFailure("Invalid response from server.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*----------------------------------------
    Trigger Cloud Function To Send Email OTP
    ----------------------------------------*/
    public void sendVerifyEmailOtp(String email, SendEmailOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("sendVerifyEmailOtp")
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

        //TODO: Delete Otp Request on Verification Success
        functions.getHttpsCallable("verifyEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*--------------------------------------------
    Trigger cloud function to mark email verified
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
    Save pending flag for unverified users (Email)
    --------------------------------------------*/
    private void markEmailUnverified(String uid) {
        db.collection("users")
                .document(uid)
                .update("emailVerifiedPending", true)
                .addOnFailureListener(e -> Log.w("EmailFlag", "Failed to set emailVerifiedPending", e));
    }

    /*------------------------------------
    Send password reset email otp to user
    -------------------------------------*/
    public void sendPasswordResetEmailOtp(String email, SendPasswordResetOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        functions.getHttpsCallable("sendPasswordResetEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /*-----------------------------------------
    Verify password reset otp provided by user
    -----------------------------------------*/
    public void verifyPasswordResetEmailOtp(String email, String otp, VerifyPasswordResetOtpCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("otp", otp);

        functions.getHttpsCallable("sendPasswordResetEmailOtp")
                .call(data)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }


    /*------------------------------------
    Sign in Method with Lockout Handling
    -------------------------------------*/
    /*
    public void signInWithEmail(String email, String password, SignInCallback callback) {
        DocumentReference attemptRef = db.collection("login_attempts").document(email);

        // Step 1: Check if lockUntil is still active
        attemptRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Timestamp lockUntil = doc.getTimestamp("lockUntil");

                        if (lockUntil != null && lockUntil.toDate().after(new Date())) {
                            callback.onFailure("Account locked. Try again later.");
                            return;
                        }
                    } else {

                    // Step 2: Proceed with Firebase sign-in
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                // Step 3: Delete login_attempts doc on success if it exists
                                if (doc.exists()) {
                                    attemptRef.delete();
                                }
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                // Step 4: On failure, increment failedAttempts or create doc
                                if (doc.exists()) {
                                    Long failedAttempts = doc.getLong("failedAttempts");
                                    failedAttempts = (failedAttempts != null) ? failedAttempts + 1 : 1;

                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("failedAttempts", failedAttempts);

                                    if (failedAttempts >= 5) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.HOUR, 1);
                                        updateData.put("lockUntil", new Timestamp(cal.getTime()));
                                    }

                                    attemptRef.set(updateData, SetOptions.merge());
                                } else {
                                    // First failure: create doc
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("failedAttempts", 1);
                                    attemptRef.set(data);
                                }

                                callback.onFailure(e.getMessage());
                            });
                    }

                })
                .addOnFailureListener(e -> {
                    // Firestore read failure fallback
                    callback.onFailure("Login failed. Try again.");
                });
    }
    */

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

    // CheckEmailExistCallback
    public interface CheckEmailExistCallback {
        void onSuccess(boolean exists);

        void onFailure(String reason);
    }

    // SendPasswordResetOtpCallback
    public interface SendPasswordResetOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }

    // VerifyPasswordResetOtpCallback
    public interface VerifyPasswordResetOtpCallback {
        void onSuccess();

        void onFailure(String reason);
    }
}