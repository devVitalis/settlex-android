package com.settlex.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.local.preference.UserPrefs;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.remote.dto.UserDto;
import com.settlex.android.util.event.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Repository managing user account session and data.
 * - Owns FirebaseAuth listener
 * - Owns Firestore snapshot listeners
 * - Exposes LiveData for auth state and user profile
 * - Ensures no duplicate listeners or stale data
 */
@Singleton
public class UserRepository {
    private final MutableLiveData<Result<UserDto>> sharedUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> sharedUserAuthState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBalanceHiddenLiveData = new MutableLiveData<>();

    // Dependencies
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final UserPrefs userPrefs;

    // Internal listeners
    private ListenerRegistration userListener;
    private ListenerRegistration transactionListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Inject
    public UserRepository(FirebaseAuth auth, FirebaseFirestore firestore, FirebaseFunctions functions, UserPrefs userPrefs) {
        this.auth = auth;
        this.firestore = firestore;
        this.functions = functions;
        this.userPrefs = userPrefs;

        // setup once (single source of truth)
        initAuthStateListener();
    }

    private void initAuthStateListener() {
        if (authStateListener != null) return; // already attached

        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            sharedUserAuthState.postValue(currentUser);

            if (currentUser == null) {
                // logged out | clear cached user
                clearUserSession();
                sharedUserLiveData.postValue(null);
                return;
            }

            // logged in → setup user listener
            initSharedUserListener(currentUser.getUid());
            initIsBalanceHiddenLiveData();
        };
        auth.addAuthStateListener(authStateListener);
    }

    public LiveData<FirebaseUser> getSharedUserAuthState() {
        // Expose current user
        return sharedUserAuthState;
    }

    private void initSharedUserListener(String uid) {
        sharedUserLiveData.postValue(Result.loading());

        Log.d("Repository", "Attaching a new user listener");

        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("Repository", "User listener error", error);
                        sharedUserLiveData.postValue(Result.error(error.getMessage()));
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

    private void initIsBalanceHiddenLiveData() {
        isBalanceHiddenLiveData.setValue(userPrefs.isBalanceHidden());
    }

    public void toggleBalanceVisibility() {
        boolean isBalanceCurrentlyHidden = userPrefs.isBalanceHidden();
        boolean shouldHideBalance = !isBalanceCurrentlyHidden;

        userPrefs.setBalanceHidden(shouldHideBalance);
        isBalanceHiddenLiveData.setValue(shouldHideBalance);
    }

    public LiveData<Boolean> getIsBalanceHiddenLiveData() {
        return isBalanceHiddenLiveData;
    }

    public void getUserTransactions(String uid, int limit, TransactionCallback callback) {
        removeTransactionListener(); // avoid multiple listeners

        Log.d("Repository", "fetching new transactions for user: " + uid);
        transactionListener = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        callback.onResult(Collections.emptyList());
                        return;
                    }

                    List<TransactionDto> transactions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TransactionDto txn = doc.toObject(TransactionDto.class);
                        if (txn != null) transactions.add(txn);
                    }
                    callback.onResult(transactions);
                });
    }

    private void removeTransactionListener() {
        if (transactionListener == null) return;
        transactionListener.remove();
        transactionListener = null;
    }

    // ------- SESSION ---------
    public void signOut() {
        auth.signOut();
    }

    private void clearUserSession() {
        removeUserListener();
        removeTransactionListener();
        // don’t remove authStateListener,
        // we want it alive to pick up next login
    }

    // -------- CALLBACKS ---------
    public interface TransactionCallback {
        void onResult(List<TransactionDto> dtolist);

        void onError(String reason);
    }
}
