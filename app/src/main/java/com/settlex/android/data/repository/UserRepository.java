package com.settlex.android.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.UserDto;
import com.settlex.android.data.remote.dto.TransactionDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages each user account
 */
public class UserRepository {
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;

    private ListenerRegistration userListener;
    private ListenerRegistration transactionsListener;

    private final MutableLiveData<UserDto> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionDto>> transactionsLiveData = new MutableLiveData<>();

    public UserRepository() {
        functions = FirebaseFunctions.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Listens to user doc in Firestore database
     */
    public LiveData<UserDto> getUser(String uid) {
        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        userLiveData.setValue(null);
                        return;
                    }
                    UserDto userDoc = snapshot.toObject(UserDto.class);
                    userLiveData.setValue(userDoc);
                });
        return userLiveData;
    }

    /**
     * Listens to recent transactions of a user
     */
    public LiveData<List<TransactionDto>> getRecentTransactions(String uid, int limit) {
        transactionsListener = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        transactionsLiveData.setValue(Collections.emptyList());
                        return;
                    }
                    List<TransactionDto> transactions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TransactionDto txn = doc.toObject(TransactionDto.class);
                        if (txn != null) transactions.add(txn);
                    }
                    transactionsLiveData.setValue(transactions);
                });

        return transactionsLiveData;
    }

    /**
     * Remove all Firestore listeners
     */
    public void removeListener() {
        if (userListener != null) userListener.remove();
        if (transactionsListener != null) transactionsListener.remove();
    }
}
