package com.settlex.android.ui.dashboard.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionOperation;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.remote.dto.UserDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.MoneyFlowUiModel;
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
    // LiveData for UI
    private final MediatorLiveData<String> authStateLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Result<UserUiModel>> userLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Result<MoneyFlowUiModel>> moneyFlowLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> uploadProfilePicLiveData = new MutableLiveData<>();

    // Dependencies
    private final UserRepository userRepo;

    @Inject
    public UserViewModel(UserRepository userRepo) {
        this.userRepo = userRepo;

        initAuthObserver();
        initUserLiveDataObserver();
        Log.d("Debug", "ViewModel Instance created: " + this);
    }

    // Public APIs
    public void toggleBalanceVisibility() {
        userRepo.toggleBalanceVisibility();
    }

    public LiveData<Boolean> getIsBalanceHiddenLiveData() {
        return userRepo.getIsBalanceHiddenLiveData();
    }

    public void signOut() {
        // Log out current user / end session
        userRepo.signOut();
    }

    public LiveData<Result<List<TransactionUiModel>>> getRecentTransactionLiveData(String uid, int limit) {
        if (transactionLiveData.getValue() != null) return transactionLiveData;

        transactionLiveData.setValue(Result.loading());
        userRepo.getUserTransactions(uid, limit, new UserRepository.TransactionCallback() {
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
                            isSender ? dto.serviceType.getDisplayName() : "Transfer received",
                            isSender ? dto.serviceType.getIconRes() : R.drawable.ic_service_money_received,
                            operation.getSymbol(),
                            operation.getColorRes(),
                            StringUtil.formatToNaira(dto.amount),
                            StringUtil.formatTimeStampToSimpleDate(dto.createdAt),
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

    public void uploadProfilePic(String profilePicUrl){
        uploadProfilePicLiveData.setValue(Result.loading());
        userRepo.uploadNewProfilePic(profilePicUrl, new UserRepository.UploadProfilePicCallback() {
            @Override
            public void onSuccess(String profilePicUrl) {
                uploadProfilePicLiveData.setValue(Result.success(profilePicUrl));
            }

            @Override
            public void onFailure(String error) {
                uploadProfilePicLiveData.setValue(Result.error(profilePicUrl));
            }
        });
    }

    public LiveData<Result<String>> getProfilePicUploadResult(){
        return uploadProfilePicLiveData;
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

    /**
     * Initializes LiveData observers and sets up data flow from repositories.
     * These setup methods are called once during ViewModel initialization.
     */
    private void initAuthObserver() {
        // Auth state → updates UID
        authStateLiveData.addSource(userRepo.getSharedUserAuthState(), user -> {
            if (user == null) {
                // Logged out
                authStateLiveData.setValue(null);
                transactionLiveData.setValue(Result.success(Collections.emptyList()));
                return;
            }
            // Logged in → set UID and attach Firestore listener
            authStateLiveData.setValue(user.getUid());
        });
    }

    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    private void initUserLiveDataObserver() {
        userLiveData.addSource(userRepo.getSharedUserLiveData(), dto -> {
            if (dto == null) {
                return;
            }

            switch (dto.getStatus()) {
                case LOADING -> userLiveData.setValue(Result.loading());
                case SUCCESS -> userLiveData.setValue(Result.success(mapToUiModel(dto.getData())));
                case ERROR -> userLiveData.setValue(Result.error(dto.getMessage()));
            }
        });
    }

    public LiveData<Result<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }

    private UserUiModel mapToUiModel(UserDto dto) {
        // User DTO → map to UI model
        return new UserUiModel(
                dto.uid,
                dto.email,
                dto.firstName,
                dto.lastName,
                dto.phone,
                dto.username,
                dto.balance,
                dto.commissionBalance,
                dto.referralBalance
        );
    }
}