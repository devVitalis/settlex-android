package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.dto.SuggestionsDto;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.SuggestionsUiModel;
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
    private final MutableLiveData<Result<List<SuggestionsUiModel>>> suggestionsLiveData = new MutableLiveData<>();


    public DashboardViewModel() {
        this.userRepo = new UserRepository();
    }

    // ====================== LIVEDATA GETTERS ======================
    public LiveData<Event<Result<String>>> getPayFriendResult() {
        return payFriendLiveData;
    }

    public LiveData<Result<List<SuggestionsUiModel>>> getUsernameSuggestion() {
        return suggestionsLiveData;
    }

    /**
     * Update the UI model for current user info
     */
    public LiveData<UserUiModel> getUserData(String uid) {
        userRepo.getUser(uid).observeForever(userDto -> {
            if (userDto == null) {
                userLiveData.setValue(null);
                return;
            }
            userLiveData.setValue(new UserUiModel(
                    userDto.uid,
                    userDto.firstName,
                    userDto.lastName,
                    userDto.username,
                    userDto.balance,
                    userDto.commissionBalance
            ));
        });
        return userLiveData;
    }

    /**
     * Expose transactions LiveData
     */
    public LiveData<List<TransactionUiModel>> getTransactions(String currentUserUid, int limit) {
        userRepo.getRecentTransactions(currentUserUid, limit).observeForever(transaction -> {
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
        });
        return transactionsLiveData;
    }

    /**
     * Initiates a peer-to-peer payment transaction from one user to another.
     */
    public void payFriend(String senderUid, String receiverUserName, String transactionId, double amount, String serviceType, String description) {
        payFriendLiveData.postValue(new Event<>(Result.loading()));
        userRepo.payFriend(senderUid, receiverUserName, transactionId, amount, serviceType, description, new UserRepository.TransferCallback() {
            @Override
            public void onTransferPending() {
                payFriendLiveData.postValue(new Event<>(Result.Pending()));
            }

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

    /**
     * Returns username query results.
     */
    public void searchUsername(String query) {
        suggestionsLiveData.postValue(Result.loading());
        userRepo.searchUsername(query, new UserRepository.SearchUsernameCallback() {
            @Override
            public void onResult(List<SuggestionsDto> suggestionsDto) {
                // Map DTO -> UI Model
                List<SuggestionsUiModel> suggestionsUiModelList = new ArrayList<>();
                for (SuggestionsDto dto : suggestionsDto) {
                    suggestionsUiModelList.add(new SuggestionsUiModel("@" + dto.username, dto.firstName + " " + dto.lastName, dto.profileUrl));
                }
                suggestionsLiveData.postValue(Result.success(suggestionsUiModelList));
            }

            @Override
            public void onFailure(String reason) {
                suggestionsLiveData.postValue(Result.success(Collections.emptyList()));
            }
        });
    }

    public void signOut() {
        userRepo.signOut();
    }

    public LiveData<FirebaseUser> getAuthState() {
        return userRepo.getAuthState();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepo.removeListener();
    }
}
