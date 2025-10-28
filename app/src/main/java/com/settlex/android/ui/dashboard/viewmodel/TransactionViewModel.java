package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.dto.RecipientDto;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.TransactionRepository;
import com.settlex.android.ui.dashboard.model.MoneyFlowUiModel;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.utils.event.Event;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class TransactionViewModel extends ViewModel {
    private final MutableLiveData<Result<List<RecipientUiModel>>> recipientSearchResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> payFriendLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Result<MoneyFlowUiModel>> moneyFlowLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionLiveData = new MutableLiveData<>();

    // dependencies
    private final TransactionRepository transactionRepo;

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public void searchRecipient(String paymentId) {
        recipientSearchResult.postValue(Result.loading());
        transactionRepo.searchRecipient(paymentId, new TransactionRepository.SearchRecipientCallback() {
            @Override
            public void onResult(List<RecipientDto> recipientDto) {
                // Map DTO -> UI Model
                List<RecipientUiModel> recipientUiModelList = new ArrayList<>();

                for (RecipientDto dto : recipientDto) {
                    recipientUiModelList.add(new RecipientUiModel(
                            StringUtil.addAtToPaymentId(dto.paymentId),
                            dto.firstName + " " + dto.lastName,
                            dto.profileUrl));
                }
                recipientSearchResult.postValue(Result.success(recipientUiModelList));
            }

            @Override
            public void onFailure(String reason) {
                recipientSearchResult.postValue(Result.error(reason));
            }
        });
    }

    public LiveData<Result<List<RecipientUiModel>>> getRecipientSearchResult() {
        return recipientSearchResult;
    }

    public void payFriend(String senderUid, String recipient, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.setValue(new Event<>(Result.loading()));
        transactionRepo.payFriend(
                senderUid,
                recipient,
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

    public LiveData<Event<Result<String>>> getPayFriendLiveData() {
        return payFriendLiveData;
    }


    public LiveData<Result<List<TransactionUiModel>>> getTransactionLiveData(String uid, int limit) {
        if (transactionLiveData.getValue() != null) return transactionLiveData;

        transactionLiveData.setValue(Result.loading());
        transactionRepo.getTransactions(uid, limit, new TransactionRepository.TransactionCallback() {
            @Override
            public void onResult(List<TransactionDto> dtolist) {
                if (dtolist == null || dtolist.isEmpty()) {
                    transactionLiveData.setValue(Result.success(Collections.emptyList()));
                    return;
                }

                initMoneyFlow(uid, dtolist);
                List<TransactionUiModel> uiModel = new ArrayList<>();
                for (TransactionDto dto : dtolist) {
                    boolean isSender = uid.equals(dto.senderUid);

                    TransactionOperation operation;
                    if (dto.status == TransactionStatus.REVERSED) {
                        operation = isSender ? TransactionOperation.CREDIT : TransactionOperation.DEBIT;
                    } else {
                        operation = isSender ? TransactionOperation.DEBIT : TransactionOperation.CREDIT;
                    }

                    uiModel.add(new TransactionUiModel(
                            dto.transactionId,
                            dto.description,
                            dto.sender,
                            dto.senderName.toUpperCase(),
                            dto.recipient,
                            dto.recipientName.toUpperCase(),
                            isSender ? dto.recipientName.toUpperCase() : dto.senderName.toUpperCase(),
                            isSender ? dto.serviceType.getDisplayName() : "Payment Received",
                            isSender ? dto.serviceType.getIconRes() : R.drawable.ic_service_payment_received,
                            operation.getSymbol(),
                            operation.getColorRes(),
                            StringUtil.formatToNaira(dto.amount),
                            StringUtil.formatTimeStampToSimpleDateAndTime(dto.createdAt),
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
        return transactionLiveData;
    }

    private void initMoneyFlow(String currentUserUid, List<TransactionDto> dtoList) {
        moneyFlowLiveData.setValue(Result.loading());
        double inFlow = 0;
        double outFlow = 0;

        if (dtoList == null) {
            // Zero transaction
            moneyFlowLiveData.setValue(null);
            return;
        }

        for (TransactionDto dto : dtoList) {

            if (dto.status != TransactionStatus.SUCCESS) {
                // Skip Failed,
                // Reversed and
                // Pending Transaction
                continue;
            }

            boolean isInFlow = !currentUserUid.equals(dto.senderUid);
            inFlow += (isInFlow) ? dto.amount : 0;
            outFlow += (isInFlow) ? 0 : dto.amount;
        }
        moneyFlowLiveData.setValue(Result.success(new MoneyFlowUiModel(inFlow, outFlow)));
    }

    public LiveData<Result<MoneyFlowUiModel>> getMoneyFlow() {
        return moneyFlowLiveData;
    }
}
