package com.settlex.android.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.TransactionDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages each user account transactions
 */
public class TransactionRepository {
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private ListenerRegistration transactionsListener;

    // LIVEDATA HOLDER

    public TransactionRepository() {
        functions = FirebaseFunctions.getInstance("europe-west2");
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Listens to recent transactions of a user
     */
    public void getRecentTransactions(String uid, int limit, TransactionsCallback callback) {
        transactionsListener = firestore.collection("users")
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

                    List<TransactionDto> transactions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TransactionDto txn = doc.toObject(TransactionDto.class);
                        if (txn != null) transactions.add(txn);
                    }
                    callback.onResult(transactions);
                });
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
        data.put("description", description);

        Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] finished = {false};

        Runnable timeoutRunnable = () -> {
            if (!finished[0]) {
                finished[0] = true;
                callback.onTransferPending();
            }
        };

        handler.postDelayed(timeoutRunnable, 10_000); // 10s timeout

        functions.getHttpsCallable("payAFriend").call(data).addOnSuccessListener(result -> {
            if (!finished[0]) {
                finished[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                callback.onTransferSuccess();
            }
        }).addOnFailureListener(e -> {
            if (!finished[0]) {
                finished[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                    callback.onTransferFailed("Network request failed. Please check your connection.");
                } else {
                    callback.onTransferFailed(e.getMessage());
                }
            }
        });
    }

    /**
     * Remove all Firestore listeners
     */
    public void removeListener() {
        if (transactionsListener != null) transactionsListener.remove();
    }

    // ============== Callbacks Interfaces
    public interface TransactionsCallback {
        void onResult(List<TransactionDto> list);

        void onError(String reason);
    }

    public interface TransferCallback {
        void onTransferPending();

        void onTransferSuccess();

        void onTransferFailed(String reason);
    }
}
