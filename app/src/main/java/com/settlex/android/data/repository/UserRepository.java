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
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.remote.dto.UserDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Manages each user account
 */
@Singleton
public class UserRepository {
    private final MutableLiveData<UserDto> userLiveData = new MutableLiveData<>();

    // Dependencies
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private ListenerRegistration userListener;
    private ListenerRegistration transactionListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Inject
    public UserRepository(FirebaseAuth auth, FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.auth = auth;
        this.firestore = firestore;
        this.functions = functions;
    }

    public void listenToUserAuthState(UserAuthStateCallback callback) {
        // Ensure only one active listener at a time
        if (authStateListener != null) {
//            auth.removeAuthStateListener(authStateListener);
            return;
        }

        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            callback.onResult(currentUser);

            if (currentUser == null) {
                // User logged out → clear snapshot listener & data
                clearUserData();
            }
        };
        auth.addAuthStateListener(authStateListener);
    }


    public void setupUserListener(String uid) {
        if (userListener != null) {
            return;
        }
        // Always clear previous listener before attaching new one
//        removeUserListener();

        Log.d("Repository", "Attaching Firestore listener for user: " + uid);
        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.w("Repository", "User listener error", error);
                        userLiveData.setValue(null);
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        userLiveData.setValue(null);
                        return;
                    }

                    userLiveData.setValue(snapshot.toObject(UserDto.class));
                });
    }

    public void getUserTransactions(String uid, int limit, TransactionCallback callback) {
        if (transactionListener != null) {
            // Listener is already active, no need to re-attach
            return;
        }
//        removeTransactionListener();

        Log.d("ViewModel", "Fetching new transactions");
        transactionListener = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit).addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        callback.onResult(Collections.emptyList());
                        return;
                    }

                    // Map data to dto
                    List<TransactionDto> transactions = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TransactionDto txn = doc.toObject(TransactionDto.class);
                        if (txn != null) transactions.add(txn);
                    }
                    callback.onResult(transactions);
                });
    }


    public void signOut() {
        Log.d("Repository", "Signing out user");
        auth.signOut();
        clearUserData();
    }

    private void removeUserListener() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    private void removeTransactionListener() {
        if (transactionListener != null) {
            transactionListener.remove();
            transactionListener = null;
        }
    }

    private void clearUserData() {
        removeUserListener();
        userLiveData.postValue(null);
    }

    // GETTERS
    public LiveData<UserDto> getUserLiveData() {
        // Shared user LiveData
        return userLiveData;
    }

    // CALLBACK INTERFACE
    public interface UserAuthStateCallback {
        void onResult(FirebaseUser user);
    }

    public interface TransactionCallback {
        void onResult(List<TransactionDto> dtolist);

        void onError(String reason);
    }
}