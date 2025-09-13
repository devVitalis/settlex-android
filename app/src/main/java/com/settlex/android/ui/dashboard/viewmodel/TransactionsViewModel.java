package com.settlex.android.ui.dashboard.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.TransactionRepository;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class TransactionsViewModel extends ViewModel {
    private final TransactionRepository transactionRepo;

    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> payFriendLiveData = new MutableLiveData<>();

    @Inject
    public TransactionsViewModel(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    // Getters =========
    public LiveData<Result<List<TransactionUiModel>>> getTransactionsLiveData() {
        return transactionsLiveData;
    }
    public LiveData<Event<Result<String>>> getPayFriendLiveData() {
        return payFriendLiveData;
    }

    /**
     * Fetch and set transactions LiveData
     */
    public void fetchUserTransactions(String currentUserUid, int limit) {
        if (transactionsLiveData.getValue() != null) return;
        Log.d("ViewModel", "fetching transactions data...");

        transactionsLiveData.setValue(Result.loading());
        transactionRepo.getUserTransactions(currentUserUid, limit, new TransactionRepository.TransactionsCallback() {
            @Override
            public void onResult(List<TransactionDto> transaction) {
                if (transaction == null || transaction.isEmpty()) {
                    transactionsLiveData.setValue(Result.success(Collections.emptyList()));
                    return;
                }

                List<TransactionUiModel> uiModel = new ArrayList<>();
                for (TransactionDto dto : transaction) {
                    boolean isSender = currentUserUid.equals(dto.senderUid); // same user

                    TransactionOperation operation;
                    if (dto.status == TransactionStatus.REVERSED) {
                        // reversed transaction is credit
                        operation = isSender ? TransactionOperation.CREDIT : TransactionOperation.DEBIT;
                    } else {
                        // sender is current user: DEBIT
                        operation = isSender ? TransactionOperation.DEBIT : TransactionOperation.CREDIT;
                    }

                    // Build UI model
                    uiModel.add(new TransactionUiModel(
                            dto.transactionId,
                            dto.sender.toUpperCase(),
                            dto.recipient.toUpperCase(),
                            isSender ? dto.recipient.toUpperCase() : dto.sender.toUpperCase(),
                            isSender ? dto.serviceType.getDisplayName() : "Transfer received",
                            dto.serviceType.getIconRes(),
                            operation.getSymbol(),
                            operation.getColorRes(),
                            StringUtil.formatToNaira(dto.amount),
                            StringUtil.formatTimeStamp(dto.createdAt),
                            dto.status.getDisplayName(),
                            dto.status.getColorRes(),
                            dto.status.getBgColorRes()
                    ));
                }
                transactionsLiveData.setValue(Result.success(uiModel));
            }

            @Override
            public void onError(String reason) {
                transactionsLiveData.setValue(Result.error("Failed to load transactions"));
            }
        });
    }

    /**
     * Initiates a peer-to-peer payment transaction from one user to another.
     */
    public void payFriend(String senderUid, String receiverUserName, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.setValue(new Event<>(Result.loading()));
        transactionRepo.payFriend(
                senderUid,
                receiverUserName,
                transactionId,
                amount,
                serviceType,
                description,
                new TransactionRepository.TransferCallback() {
                    @Override
                    public void onTransferPending() {
                        payFriendLiveData.setValue(new Event<>(Result.Pending("Transaction Pending")));
                    }

                    @Override
                    public void onTransferSuccess() {
                        payFriendLiveData.setValue(new Event<>(Result.success("Transaction Successful")));
                    }

                    @Override
                    public void onTransferFailed(String reason) {
                        payFriendLiveData.setValue(new Event<>(Result.error("Transaction Failed")));
                    }
                });
    }

    @Override
    protected void onCleared() {
        Log.d("ViewModel", "Clearing data...");
        super.onCleared();
        transactionRepo.removeListener();
    }
}
