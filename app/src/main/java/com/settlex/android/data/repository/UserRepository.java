package com.settlex.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.SettleXApp;
import com.settlex.android.data.local.UserPrefs;
import com.settlex.android.data.remote.dto.UserDto;
import com.settlex.android.utils.event.Result;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserRepository {
    private final String TAG = UserRepository.class.getSimpleName();
    private final String ERROR_NO_INTERNET = "Connection lost. Please check your Wi-Fi or cellular data and try again";
    private final String ERROR_FALLBACK = "Something went wrong. Try again";

    private final MutableLiveData<Result<UserDto>> sharedUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> sharedUserAuthState = new MutableLiveData<>();

    // Dependencies
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private UserPrefs userPrefs;
    private final TransactionRepository transactionRepo;

    // Internal listeners
    private ListenerRegistration userListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Inject
    public UserRepository(FirebaseAuth auth, FirebaseFirestore firestore, FirebaseFunctions functions, TransactionRepository transactionRepo) {
        this.auth = auth;
        this.firestore = firestore;
        this.functions = functions;
        this.transactionRepo = transactionRepo;

        // setup once (single source of truth)
        initAuthStateListener();
        Log.d(TAG, "user repo is active: " + this);
    }

    private void initAuthStateListener() {
        if (authStateListener != null) return;

        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            sharedUserAuthState.postValue(currentUser);

            if (currentUser != null) {
                initSharedUserListener(currentUser.getUid());
                userPrefs = new UserPrefs(SettleXApp.getAppContext(), currentUser.getUid());
                return;
            }

            // Logged out | clear cached user
            clearUserSession();
            userPrefs = null;
            sharedUserLiveData.postValue(null);
        };
        auth.addAuthStateListener(authStateListener);
    }

    public LiveData<FirebaseUser> getSharedUserAuthState() {
        // Expose user auth state
        return sharedUserAuthState;
    }

    private void initSharedUserListener(String uid) {
        sharedUserLiveData.postValue(Result.loading());

        Log.d(TAG, "Attaching a new user listener");

        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "User listener error" + error.getMessage(), error);
                        sharedUserLiveData.postValue(Result.failure(error.getMessage()));
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        sharedUserLiveData.postValue(null);
                        return;
                    }
                    sharedUserLiveData.postValue(Result.success(snapshot.toObject(UserDto.class)));
                });
    }

    public LiveData<Result<UserDto>> getSharedUserLiveData() {
        // Expose user data
        return sharedUserLiveData;
    }

    private void removeUserListener() {
        if (userListener == null) return;
        userListener.remove();
        userListener = null;
    }

    // user preference
    private UserPrefs getUserPrefs() {
        return userPrefs;
    }

    public boolean getPayBiometricsEnabled() {
        return getUserPrefs().isPayBiometricsEnabled();
    }

    public void setPayBiometricsEnabled(boolean enable) {
        getUserPrefs().setPayBiometricsEnabled(enable);
    }

    public boolean getLoginBiometricsEnabled() {
        return getUserPrefs().isLoginBiometricsEnabled();
    }

    public void setLoginBiometricsEnabled(boolean enable) {
        getUserPrefs().setLoginBiometricsEnabled(enable);
    }

    public boolean getBalanceHidden() {
        return getUserPrefs().isBalanceHidden();
    }

    public void toggleBalanceVisibility(boolean shouldHideBalance) {
        getUserPrefs().setBalanceHidden(shouldHideBalance);
    }

    public void uploadUserProfilePicToServer(String imageBase64, UploadProfilePicCallback callback) {
        functions.getHttpsCallable("default-uploadProfilePicture")
                .call(Collections.singletonMap("imageBase64", imageBase64))
                .addOnSuccessListener(result -> {
                    callback.onSuccess();
                    reloadUser();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Failed to upload picture: " + e.getMessage(), e);
                });
    }

    public interface UploadProfilePicCallback {
        void onSuccess();

        void onFailure(String error);
    }

    private void reloadUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        user.reload().addOnFailureListener(e ->
                Log.e(TAG, "Failed to reload user: " + e.getMessage(), e));

    }

    public void checkPaymentIdAvailability(String paymentId, PaymentIdAvailableCallback callback) {
        firestore.collection("payment_ids")
                .document(paymentId)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.exists()))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "Failed to check Payment ID availability " + e.getMessage(), e);
                });
    }

    public interface PaymentIdAvailableCallback {
        void onSuccess(boolean exists);

        void onFailure(String error);
    }

    public void storePaymentId(String paymentId, String uid, StorePaymentIdCallback callback) {
        DocumentReference globalDocRef = firestore.collection("payment_ids").document(paymentId);
        DocumentReference userDocRef = firestore.collection("users").document(uid);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot docSnapshot = transaction.get(globalDocRef);

                    if (docSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Payment ID is already taken", FirebaseFirestoreException.Code.ABORTED);
                    }

                    transaction.set(globalDocRef, Collections.singletonMap("uid", uid));
                    transaction.set(userDocRef, Collections.singletonMap("paymentId", paymentId), SetOptions.merge());
                    return null;
                })
                .addOnSuccessListener(success -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }

                    if (e instanceof FirebaseFirestoreException eff) {
                        if (eff.getCode() == FirebaseFirestoreException.Code.ABORTED) {
                            callback.onFailure(e.getMessage());
                            return;
                        }
                    }
                    callback.onFailure(ERROR_FALLBACK);
                    Log.e(TAG, "failed to store user payment ID: " + e.getMessage(), e);
                });
    }

    public interface StorePaymentIdCallback {
        void onSuccess();

        void onFailure(String error);
    }

    public void createPaymentPin(String pin, CreatePaymentPinCallback callback) {
        functions.getHttpsCallable("default-setPaymentPin")
                .call(Collections.singletonMap("pin", pin))
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onError(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onError(ERROR_FALLBACK);
                    Log.e(TAG, "Failed to create Payment PIN: " + e.getMessage(), e);
                });
    }

    public interface CreatePaymentPinCallback {
        void onSuccess();

        void onError(String error);
    }

    public void VerifyPaymentPin(String pin, VerifyPaymentPinCallback callback) {
        functions.getHttpsCallable("default-validatePaymentPin")
                .call(Collections.singletonMap("pin", pin))
                .addOnSuccessListener(result -> {
                    Map<?, ?> data = (Map<?, ?>) result.getData();
                    if (data != null) {
                        boolean isVerified = (boolean) data.get("verified");
                        callback.onSuccess(isVerified);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onError(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onError(ERROR_FALLBACK);
                    Log.e(TAG, "Failed to verify user Payment PIN: " + e.getMessage(), e);
                });
    }

    public interface VerifyPaymentPinCallback {
        void onSuccess(boolean isVerified);

        void onError(String error);
    }

    public void updatePassword(String email, String oldPassword, String newPassword, UpdatePasswordCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, oldPassword);
        if (user != null) {
            // Validate old password
            user.reauthenticate(authCredential)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                                callback.onFailure(ERROR_NO_INTERNET);
                                return;
                            }

                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                callback.onFailure("Oops! The Current Password you entered doesn't match our records. Please check it and try again.");
                            }
                            return;
                        }

                        // Set new password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(unused -> callback.onSuccess())
                                .addOnFailureListener(e -> {
                                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                                        callback.onFailure(ERROR_NO_INTERNET);
                                        return;
                                    }

                                    callback.onFailure(ERROR_FALLBACK);
                                });
                    });
        }
    }

    public interface UpdatePasswordCallback {
        void onSuccess();

        void onFailure(String error);
    }

    public void signOut() {
        // end session
        auth.signOut();
    }

    private void clearUserSession() {
        removeUserListener();
        transactionRepo.removeTransactionListener();
        // don’t remove authStateListener,
        // we want it alive to pick up next login
    }
}
