package com.settlex.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.remote.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        functions = FirebaseFunctions.getInstance("europe-west2");
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
                .orderBy("createdAt", Query.Direction.DESCENDING)
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

    /**
     * Performs an Internal transfer btw users
     */
    public void payFriend(String senderUid, String receiverUsername, String transactionId, double amount, String serviceType, String description, TransferCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("senderUid", senderUid);
        data.put("receiverUsername", receiverUsername);
        data.put("transactionId", transactionId);
        data.put("amount", amount);
        data.put("serviceType", serviceType);
        data.put("description", description); // Nullable

        // logging to see what client is sending
        Log.d("TransferFunds", "Sending data:" + data);

        functions.getHttpsCallable("transferFunds")
                .call(data)
                .addOnSuccessListener(result -> callback.onTransferSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onTransferFailed("Network request failed. Please check your network and try again");
                        return;
                    }
                    callback.onTransferFailed(e.getMessage());
                });

    }

    // ============== Callbacks Interfaces
    public interface TransferCallback {
        void onTransferSuccess();
        void onTransferFailed(String reason);
    }
}
