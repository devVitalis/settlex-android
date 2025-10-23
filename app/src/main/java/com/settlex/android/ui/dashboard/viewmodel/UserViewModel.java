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
import com.settlex.android.utils.event.Event;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final MediatorLiveData<String> authStateLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Result<UserUiModel>> userLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Result<MoneyFlowUiModel>> moneyFlowLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Result<List<TransactionUiModel>>> transactionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> uploadProfilePicLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Boolean>>> checkPaymentIdExistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> storeUserPaymentIdLiveData = new MutableLiveData<>();

    // dependencies
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

    public void uploadProfilePic(String imageBase64) {
        uploadProfilePicLiveData.setValue(Result.loading());
        userRepo.uploadUserProfilePicToServer(imageBase64, new UserRepository.UploadProfilePicCallback() {
            @Override
            public void onSuccess() {
                uploadProfilePicLiveData.setValue(Result.success("Profile changed successful"));
            }

            @Override
            public void onFailure(String error) {
                uploadProfilePicLiveData.setValue(Result.error(error));
            }
        });
    }

    public LiveData<Result<String>> getProfilePicUploadResult() {
        return uploadProfilePicLiveData;
    }

    public void checkPaymentIdExists(String paymentId) {
        checkPaymentIdExistsLiveData.setValue(new Event<>(Result.loading()));
        userRepo.checkPaymentIdAvailability(paymentId, new UserRepository.PaymentIdAvailableCallback() {
            @Override
            public void onSuccess(boolean exists) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(Result.success(exists)));
            }

            @Override
            public void onFailure(String error) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(Result.error(error)));
            }
        });
    }

    public LiveData<Event<Result<Boolean>>> getPaymentIdExistsStatus() {
        return checkPaymentIdExistsLiveData;
    }

    public void storeUserPaymentIdToServer(String paymentId, String uid) {
        storeUserPaymentIdLiveData.setValue(new Event<>(Result.loading()));
        userRepo.storeUserPaymentIdToDatabase(paymentId, uid, new UserRepository.StorePaymentIdCallback() {
            @Override
            public void onSuccess() {
                storeUserPaymentIdLiveData.setValue(new Event<>(Result.success("success")));
            }

            @Override
            public void onFailure(String error) {
                storeUserPaymentIdLiveData.setValue(new Event<>(Result.error(error)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getStoreUserPaymentIdStatus() {
        return storeUserPaymentIdLiveData;
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

    private UserUiModel mapToUiModel(UserDto dto) {
        // User DTO → map to UI model
        return new UserUiModel(
                dto.uid,
                dto.email,
                dto.firstName,
                dto.lastName,
                dto.createdAt,
                dto.phone,
                dto.paymentId,
                dto.profileUrl,
                dto.profileDeleteUrl,
                dto.balance,
                dto.commissionBalance,
                dto.referralBalance
        );
    }

    public LiveData<Result<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }
}