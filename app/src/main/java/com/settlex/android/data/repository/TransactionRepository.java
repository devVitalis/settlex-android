package com.settlex.android.data.repository;

import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.RecipientDto;
import com.settlex.android.data.remote.dto.TransactionDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

public class TransactionRepository {
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private ListenerRegistration transactionListener;

    @Inject
    public TransactionRepository(FirebaseFunctions functions, FirebaseFirestore firestore) {
        this.functions = functions;
        this.firestore = firestore;
    }

    public void removeListener() {
        if (transactionListener != null) transactionListener.remove();
    }

    public void getUserTransactions(String uid, int limit, TransactionHistoryCallback callback) {
        if (transactionListener != null) {
            // Listener is already active, no need to re-attach
            return;
        }
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

    public void searchRecipientWithUsername(String input, SearchRecipientCallback callback) {
        functions.getHttpsCallable("searchUsername")
                .call(Collections.singletonMap("input", input)) // TODO rename
                .addOnSuccessListener(result -> {

                    List<RecipientDto> recipientDto = new ArrayList<>();
                    Map<?, ?> data = (Map<?, ?>) result.getData(); // get res data
                    if (data != null && Boolean.TRUE.equals(data.get("success"))) {
                        //noinspection unchecked
                        List<Map<String, Object>> dto = (List<Map<String, Object>>) data.get("suggestions");

                        if (dto != null) {
                            for (Map<String, Object> recipient : dto) {
                                String username = (String) recipient.get("username");
                                String firstName = (String) recipient.get("firstName");
                                String lastName = (String) recipient.get("lastName");
                                String profileUrl = (String) recipient.get("profileUrl");
                                recipientDto.add(new RecipientDto(username, firstName, lastName, profileUrl));
                            }
                        }
                    }
                    callback.onResult(recipientDto);
                }).addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure("Network request failed. Please check your network and try again");
                        return;
                    }
                    callback.onFailure(e.getMessage());
                });
    }

    public void payFriend(String senderUid, String receiverUsername, String transactionId, double amount,
                          String serviceType, String description, PayFriendCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("senderUid", senderUid);
        data.put("receiverUsername", receiverUsername); // TODO rename
        data.put("transactionId", transactionId);
        data.put("amount", amount);
        data.put("serviceType", serviceType);
        data.put("description", description);

        functions.getHttpsCallable("payAFriend")
                .call(data)
                .addOnSuccessListener(result -> callback.onPayFriendSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onPayFriendFailed("Network request failed. Please check your connection.");
                        return;
                    }
                    callback.onPayFriendFailed(e.getMessage());
                });
    }

    // Callbacks Interfaces
    public interface TransactionHistoryCallback {
        void onResult(List<TransactionDto> dtolist);

        void onError(String reason);
    }

    public interface SearchRecipientCallback {
        void onResult(List<RecipientDto> dto);

        void onFailure(String reason);
    }

    public interface PayFriendCallback {
        void onPayFriendSuccess();

        void onPayFriendFailed(String reason);
    }
}
