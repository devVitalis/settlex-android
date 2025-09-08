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

public class TransactionsViewModel extends ViewModel {

    private final TransactionRepository transactionRepo;

    // LiveData holders
    private final MutableLiveData<List<TransactionUiModel>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> payFriendLiveData = new MutableLiveData<>();


    public TransactionsViewModel() {
        this.transactionRepo = new TransactionRepository();

    }

    //  GETTERS ====================
    public LiveData<Event<Result<String>>> getPayFriendLiveData() {
        return payFriendLiveData;
    }

    public LiveData<List<TransactionUiModel>> getTransactions() {
        return transactionsLiveData;
    }

    /**
     * Expose transactions LiveData
     */
    public void fetchTransactions(String currentUserUid, int limit) {
        if (transactionsLiveData.getValue() != null && !transactionsLiveData.getValue().isEmpty()) return;
        transactionRepo.getRecentTransactions(currentUserUid, limit, new TransactionRepository.TransactionsCallback() {
            @Override
            public void onResult(List<TransactionDto> transaction) {
                if (transaction == null || transaction.isEmpty()) {
                    transactionsLiveData.setValue(Collections.emptyList());
                    return;
                }

                List<TransactionUiModel> uiList = new ArrayList<>();
                for (TransactionDto dto : transaction) {
                    boolean isSender = currentUserUid.equals(dto.senderUid);

                    TransactionOperation operation;
                    if (dto.status == TransactionStatus.REVERSED) {
                        operation = isSender ? TransactionOperation.CREDIT : TransactionOperation.DEBIT;
                    } else {
                        operation = isSender ? TransactionOperation.DEBIT : TransactionOperation.CREDIT;
                    }

                    // Build UI model
                    uiList.add(new TransactionUiModel(
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
                            dto.status.getColorRes()
                    ));
                }
                transactionsLiveData.setValue(uiList);
            }

            @Override
            public void onFailure(String reason) {
                transactionsLiveData.setValue(Collections.emptyList());
            }
        });
    }

    /**
     * Initiates a peer-to-peer payment transaction from one user to another.
     */
    public void payFriend(String senderUid, String receiverUserName, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.postValue(new Event<>(Result.loading()));
        transactionRepo.payFriend(senderUid, receiverUserName, transactionId, amount, serviceType, description, new TransactionRepository.TransferCallback() {
            @Override
            public void onTransferPending() {
                payFriendLiveData.postValue(new Event<>(Result.Pending("Transaction Pending")));
            }

            @Override
            public void onTransferSuccess() {
                payFriendLiveData.postValue(new Event<>(Result.success("Transaction Successful")));
            }

            @Override
            public void onTransferFailed(String reason) {
                payFriendLiveData.postValue(new Event<>(Result.error("Transaction Failed")));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        transactionRepo.removeListener();
    }
}
