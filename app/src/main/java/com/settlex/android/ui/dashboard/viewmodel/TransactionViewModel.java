package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.dto.RecipientDto;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.TransactionRepository;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
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
public class TransactionViewModel extends ViewModel {
    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<List<RecipientUiModel>>> recipientSearchResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> payFriendLiveData = new MutableLiveData<>();

    // dependencies
    private final TransactionRepository transactionRepo;

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        transactionRepo.removeListener();
    }

    public void getUserTransactions(String currentUserUid, int limit) {
        if (transactionLiveData.getValue() != null) return;

        transactionLiveData.setValue(Result.loading());

        transactionRepo.getUserTransactions(currentUserUid, limit, new TransactionRepository.TransactionHistoryCallback() {
            @Override
            public void onResult(List<TransactionDto> dtolist) {
                if (dtolist == null || dtolist.isEmpty()) {
                    transactionLiveData.setValue(Result.success(Collections.emptyList()));
                    return;
                }

                List<TransactionUiModel> uiModel = new ArrayList<>();
                for (TransactionDto dto : dtolist) {
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
                transactionLiveData.setValue(Result.success(uiModel));
            }

            @Override
            public void onError(String reason) {
                transactionLiveData.setValue(Result.error("Failed to load transactions"));
            }
        });
    }

    public void searchRecipientWithUsername(String query) {
        recipientSearchResult.postValue(Result.loading());
        transactionRepo.searchRecipientWithUsername(query, new TransactionRepository.SearchRecipientCallback() {
            @Override
            public void onResult(List<RecipientDto> recipientDto) {
                // Map DTO -> UI Model
                List<RecipientUiModel> recipientUiModelList = new ArrayList<>();

                for (RecipientDto dto : recipientDto) {
                    recipientUiModelList.add(new RecipientUiModel(
                            StringUtil.addAtToUsername(dto.username),
                            dto.firstName + " " + dto.lastName,
                            dto.profileUrl)); // Null on default
                }
                recipientSearchResult.postValue(Result.success(recipientUiModelList));
            }

            @Override
            public void onFailure(String reason) {
                // Post a dedicated error state.
                recipientSearchResult.postValue(Result.error(reason));
            }
        });
    }

    public void payFriend(String senderUid, String receiverUserName, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.setValue(new Event<>(Result.loading()));
        transactionRepo.payFriend(
                senderUid,
                receiverUserName,
                transactionId,
                amount,
                serviceType,
                description,
                new TransactionRepository.PayFriendCallback() {
                    @Override
                    public void onPayFriendSuccess() {
                        payFriendLiveData.setValue(new Event<>(Result.success("Transaction Successful")));
                    }

                    @Override
                    public void onPayFriendFailed(String reason) {
                        payFriendLiveData.setValue(new Event<>(Result.error("Transaction Failed")));
                    }
                });
    }

    // Getters =========
    public LiveData<Result<List<TransactionUiModel>>> getTransactionLiveData() {
        return transactionLiveData;
    }

    public LiveData<Result<List<RecipientUiModel>>> getRecipientSearchResult() {
        return recipientSearchResult;
    }

    public LiveData<Event<Result<String>>> getPayFriendLiveData() {
        return payFriendLiveData;
    }
}
