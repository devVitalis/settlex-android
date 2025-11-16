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
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.UiState;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class TransactionViewModel extends ViewModel {
    private final MutableLiveData<UiState<List<RecipientUiModel>>> recipientSearchResult = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<java.lang.String>>> transferFundsLiveData = new MutableLiveData<>();
    private final MediatorLiveData<UiState<MoneyFlowUiModel>> moneyFlowLiveData = new MediatorLiveData<>();
    private final MutableLiveData<UiState<List<TransactionUiModel>>> transactionLiveData = new MutableLiveData<>();

    // dependencies
    private final TransactionRepository txnRepo;

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepo) {
        this.txnRepo = transactionRepo;
    }

    public void findRecipientByPaymentId(java.lang.String paymentId) {
        recipientSearchResult.postValue(UiState.loading());
        txnRepo.findRecipientByPaymentId(paymentId, new TransactionRepository.SearchRecipientCallback() {
            @Override
            public void onResult(List<RecipientDto> recipientDto) {
                // Map DTO -> UI Model
                List<RecipientUiModel> recipientUiModelList = new ArrayList<>();

                for (RecipientDto dto : recipientDto) {
                    recipientUiModelList.add(new RecipientUiModel(
                            StringFormatter.addAtToPaymentId(dto.paymentId),
                            dto.firstName + " " + dto.lastName,
                            dto.photoUrl));
                }
                recipientSearchResult.postValue(UiState.success(recipientUiModelList));
            }

            @Override
            public void onFailure(java.lang.String reason) {
                recipientSearchResult.postValue(UiState.failure(reason));
            }
        });
    }

    public LiveData<UiState<List<RecipientUiModel>>> getRecipientSearchResult() {
        return recipientSearchResult;
    }

    public void transferFunds(java.lang.String senderUid, java.lang.String recipient, java.lang.String transactionId, long amount, java.lang.String serviceType, java.lang.String description) {
        transferFundsLiveData.setValue(new Event<>(UiState.loading()));

        txnRepo.transferFunds(senderUid, recipient, transactionId, amount, serviceType, description,
                new TransactionRepository.PayFriendCallback() {
                    @Override
                    public void onPayFriendSuccess() {
                        transferFundsLiveData.setValue(new Event<>(UiState.success("Transaction Successful")));
                    }

                    @Override
                    public void onPayFriendFailed(java.lang.String reason) {
                        transferFundsLiveData.setValue(new Event<>(UiState.failure("Transaction Failed")));
                    }
                });
    }

    public LiveData<Event<UiState<java.lang.String>>> getTransferFundsLiveData() {
        return transferFundsLiveData;
    }


    public LiveData<UiState<List<TransactionUiModel>>> fetchTransactionsLiveData(java.lang.String uid, int limit) {
        if (transactionLiveData.getValue() != null) return transactionLiveData;

        transactionLiveData.setValue(UiState.loading());
        txnRepo.fetchTransactions(uid, limit, new TransactionRepository.TransactionCallback() {
            @Override
            public void onResult(List<TransactionDto> dtolist) {
                if (dtolist == null || dtolist.isEmpty()) {
                    transactionLiveData.setValue(UiState.success(Collections.emptyList()));
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
                            CurrencyFormatter.formatToNaira(dto.amount),
                            StringFormatter.formatTimeStampToSimpleDateAndTime(dto.createdAt),
                            dto.status.getDisplayName(),
                            dto.status.getColorRes(),
                            dto.status.getBgColorRes()
                    ));
                }
                transactionLiveData.setValue(UiState.success(uiModel));
            }

            @Override
            public void onError(java.lang.String reason) {
                transactionLiveData.setValue(UiState.failure("Failed to load transactions"));
            }
        });
        return transactionLiveData;
    }

    private void initMoneyFlow(java.lang.String currentUserUid, List<TransactionDto> dtoList) {
        moneyFlowLiveData.setValue(UiState.loading());
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
        moneyFlowLiveData.setValue(UiState.success(new MoneyFlowUiModel(inFlow, outFlow)));
    }

    public LiveData<UiState<MoneyFlowUiModel>> getMoneyFlow() {
        return moneyFlowLiveData;
    }
}
