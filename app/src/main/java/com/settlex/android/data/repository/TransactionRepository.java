package com.settlex.android.data.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.RecipientDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

public class TransactionRepository {
    private final String ERROR_NO_INTERNET = "Connection lost. Please check your Wi-Fi or cellular data and try again";

    // dependencies
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;

    @Inject
    public TransactionRepository(FirebaseFunctions functions, FirebaseFirestore firestore) {
        this.functions = functions;
        this.firestore = firestore;
    }

    public void searchRecipient(String paymentId, SearchRecipientCallback callback) {
        functions.getHttpsCallable("searchPaymentId")
                .call(Collections.singletonMap("paymentId", paymentId))
                .addOnSuccessListener(result -> {
                    Map<?, ?> data = (Map<?, ?>) result.getData();
                    List<RecipientDto> recipientDto = new ArrayList<>();

                    if (data != null && Boolean.TRUE.equals(data.get("success"))) {
                        //noinspection unchecked
                        List<Map<String, Object>> dtoList = (List<Map<String, Object>>) data.get("recipient");

                        if (dtoList != null) {
                            ObjectMapper mapper = new ObjectMapper();
                            recipientDto = dtoList.stream()
                                    .map(item -> mapper.convertValue(item, RecipientDto.class))
                                    .collect(Collectors.toList());
                        }
                    }
                    callback.onResult(recipientDto);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onFailure(e.getMessage());
                });
    }

    public interface SearchRecipientCallback {
        void onResult(List<RecipientDto> dto);
        void onFailure(String reason);
    }

    public void payFriend(String senderUid, String recipient, String transactionId, double amount, String serviceType, String description, PayFriendCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("senderUid", senderUid);
        data.put("recipient", recipient);
        data.put("transactionId", transactionId);
        data.put("amount", amount);
        data.put("serviceType", serviceType);
        data.put("description", description);

        functions.getHttpsCallable("payAFriend")
                .call(data).addOnSuccessListener(result -> callback.onPayFriendSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onPayFriendFailed(ERROR_NO_INTERNET);
                        return;
                    }
                    callback.onPayFriendFailed(e.getMessage());
                });
    }

    public interface PayFriendCallback {
        void onPayFriendSuccess();
        void onPayFriendFailed(String reason);
    }
}
