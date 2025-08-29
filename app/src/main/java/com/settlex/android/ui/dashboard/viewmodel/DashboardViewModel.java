package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final UserRepository userRepo;

    // LiveData holders
    private final MutableLiveData<UserUiModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionUiModel>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> payFriendLiveData = new MutableLiveData<>();

    public DashboardViewModel() {
        this.userRepo = new UserRepository();
    }

    // ====================== LIVEDATA GETTERS ======================
    public LiveData<Event<Result<String>>> getPayFriendResult() {
        return payFriendLiveData;
    }

    /**
     * Update the UI model for user info
     */
    public LiveData<UserUiModel> getUser(String uid) {
        double MILLION_THRESHOLD = 999_999_999;
        userRepo.getUser(uid).observeForever(userDto -> {
            if (userDto == null) {
                userLiveData.setValue(null);
                return;
            }
            userLiveData.setValue(new UserUiModel(
                    userDto.firstName,
                    userDto.lastName,
                    userDto.balance > MILLION_THRESHOLD ? StringUtil.formatToNairaShort(userDto.balance) : StringUtil.formatToNaira(userDto.balance),
                    StringUtil.formatToNairaShort(userDto.commissionBalance)
            ));
        });
        return userLiveData;
    }

    /**
     * Expose transactions LiveData
     */
    public LiveData<List<TransactionUiModel>> getTransactions(String uid, int limit) {
        // Observe DTOs and map to UI
        userRepo.getRecentTransactions(uid, limit).observeForever(transactionDto -> {
            if (transactionDto == null || transactionDto.isEmpty()) {
                transactionsLiveData.setValue(Collections.emptyList());
                return;
            }
            List<TransactionUiModel> uiList = new ArrayList<>();
            for (TransactionDto dto : transactionDto) {
                // Format to UI ready
                uiList.add(new TransactionUiModel(
                        dto.sender.toUpperCase(),
                        dto.recipient.toUpperCase(),
                        dto.serviceType == TransactionServiceType.SEND_MONEY
                                ? dto.serviceType.getDisplayName() + dto.recipient
                                : dto.serviceType == TransactionServiceType.RECEIVE_MONEY
                                ? dto.serviceType.getDisplayName() + dto.sender
                                : dto.serviceType.getDisplayName(),
                        dto.serviceType.getIconRes(),
                        dto.operation.getSymbol(),
                        dto.operation.getColorRes(),
                        StringUtil.formatToNaira(dto.amount),
                        StringUtil.formatTimeStamp(dto.createdAt),
                        dto.status.getDisplayName(),
                        dto.status.getColorRes()
                ));
            }
            transactionsLiveData.setValue(uiList);
        });
        return transactionsLiveData;
    }

    /**
     * Initiates a peer-to-peer payment transaction from one user to another.
     */
    public void payFriend(String senderUid, String receiverUserName, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.setValue(new Event<>(Result.loading()));
        userRepo.payFriend(senderUid, receiverUserName, transactionId, amount, serviceType, description, new UserRepository.TransferCallback() {
            @Override
            public void onTransferSuccess() {
                payFriendLiveData.postValue(new Event<>(Result.success("Transfer Success")));
            }
            @Override
            public void onTransferFailed(String reason) {
                payFriendLiveData.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepo.removeListener();
    }
}
