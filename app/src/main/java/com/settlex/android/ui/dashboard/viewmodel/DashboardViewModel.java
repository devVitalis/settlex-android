package com.settlex.android.ui.dashboard.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.remote.dto.TransactionDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardViewModel extends ViewModel {
    private final UserRepository userRepo;

    // LiveData holders
    private final MutableLiveData<UserUiModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionDto>> transactionsLiveData = new MutableLiveData<>();

    // Cached data
    private List<TransactionDto> cachedTransactions = new ArrayList<>();

    public DashboardViewModel() {
        this.userRepo = new UserRepository();
    }

    /**
     * Expose LiveData<UserUiModel> to the UI
     * Update the UI model for user info
     */
    @Nullable
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
    public LiveData<List<TransactionDto>> getTransactions(String uid, int limit) {
        userRepo.getRecentTransactions(uid, limit).observeForever(transactions -> {
            cachedTransactions = transactions != null ? transactions : Collections.emptyList();
            transactionsLiveData.setValue(cachedTransactions);
        });
        return transactionsLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        userRepo.removeListener();
    }
}
