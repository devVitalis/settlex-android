package com.settlex.android.ui.dashboard.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.local.preference.UserPrefs;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final MutableLiveData<String> authStateLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Result<UserUiModel>> userLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBalanceHiddenLiveData = new MutableLiveData<>();

    // Dependencies
    private final UserPrefs userPrefs;
    private final UserRepository userRepo;

    @Inject
    public UserViewModel(UserRepository userRepo, UserPrefs userPrefs) {
        this.userRepo = userRepo;
        this.userPrefs = userPrefs;

        initUserAuthState();
        initUserUiLiveData();
        initIsBalanceHiddenLiveData();

        Log.d("ViewModel", "new instance created: " + this);
    }

    // ---------------- PUBLIC API ----------------
    public void toggleBalanceVisibility() {
        boolean isBalanceCurrentlyHidden = Boolean.TRUE.equals(isBalanceHiddenLiveData.getValue());
        boolean shouldHideBalance = !isBalanceCurrentlyHidden;

        userPrefs.setBalanceHidden(shouldHideBalance);
        isBalanceHiddenLiveData.setValue(shouldHideBalance);
    }

    public void signOut() {
        userRepo.signOut();
    }

    public LiveData<Result<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsBalanceHiddenLiveData() {
        return isBalanceHiddenLiveData;
    }

    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    // ---------------- INITIALIZERS ----------------
    private void initIsBalanceHiddenLiveData() {
        isBalanceHiddenLiveData.setValue(userPrefs.isBalanceHidden());
    }

    private void initUserAuthState() {
        userRepo.listenToUserAuthState(user -> {
            if (user == null) {
                authStateLiveData.setValue(null);
                userLiveData.setValue(null);
                transactionsLiveData.setValue(null);
                return;
            }
            authStateLiveData.setValue(user.getUid());
            userRepo.setupUserListener(user.getUid());
        });
    }

    private void initUserUiLiveData() {
        userLiveData.setValue(Result.loading());

        userLiveData.addSource(userRepo.getUserLiveData(), dto -> {
            if (dto == null) {
                userLiveData.setValue(null);
                return;
            }
            UserUiModel uiModel = new UserUiModel(
                    dto.uid,
                    dto.firstName,
                    dto.lastName,
                    dto.username,
                    dto.balance,
                    dto.commissionBalance
            );
            userLiveData.setValue(Result.success(uiModel));
        });
    }

    // Expose transaction LiveData
    public LiveData<Result<List<TransactionUiModel>>> getRecentTransactionLiveData(String uid, int limit) {
        transactionsLiveData.setValue(Result.loading());

        userRepo.getUserTransactions(uid, limit, new UserRepository.TransactionCallback() {
            @Override
            public void onResult(List<TransactionDto> dtolist) {
                if (dtolist == null || dtolist.isEmpty()) {
                    transactionsLiveData.setValue(Result.success(Collections.emptyList()));
                    return;
                }

                List<TransactionUiModel> uiModel = new ArrayList<>();
                for (TransactionDto dto : dtolist) {
                    boolean isSender = uid.equals(dto.senderUid); // same user

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
                            dto.description,
                            dto.sender,
                            dto.senderName.toUpperCase(),
                            dto.recipient,
                            dto.recipientName.toUpperCase(),
                            isSender ? dto.recipientName.toUpperCase() : dto.senderName.toUpperCase(),
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
        return transactionsLiveData;
    }
}
