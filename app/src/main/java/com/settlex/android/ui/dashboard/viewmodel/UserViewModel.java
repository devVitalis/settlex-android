package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.local.preference.UserPrefs;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.event.Result;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final UserPrefs userPrefs;
    private final UserRepository userRepo;

    // LiveData holders
    private final MutableLiveData<String> authStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<UserUiModel>> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBalanceHiddenLiveData = new MutableLiveData<>();

    @Inject
    public UserViewModel(UserRepository userRepo, UserPrefs userPrefs) {
        this.userRepo = userRepo;
        this.userPrefs = userPrefs;

        initUserAuthState();
        initUserUiLiveData();
        initIsBalanceHiddenLiveData();
    }

    public void toggleBalanceVisibility() {
        boolean isBalanceCurrentlyHidden = Boolean.TRUE.equals(isBalanceHiddenLiveData.getValue());
        boolean shouldHideBalance = !isBalanceCurrentlyHidden;

        // Update new state
        userPrefs.setBalanceHidden(shouldHideBalance);
        isBalanceHiddenLiveData.setValue(shouldHideBalance);
    }

    public void signOut() {
        userRepo.signOut();
    }

    // Getters
    public LiveData<Result<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsBalanceHiddenLiveData() {
        return isBalanceHiddenLiveData;
    }

    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    // initialize one-time user services
    private void initIsBalanceHiddenLiveData() {
        isBalanceHiddenLiveData.setValue(userPrefs.isBalanceHidden());
    }

    private void initUserAuthState() {
        // Set up the listener on repository
        userRepo.listenToUserAuthState(user -> {
            if (user == null) {
                authStateLiveData.setValue(null);
                userLiveData.setValue(null);
                userRepo.removeListeners();
                return;
            }
            authStateLiveData.setValue(user.getUid());
            userRepo.setupUserListener(user.getUid());
        });
    }

    private void initUserUiLiveData() {
        userLiveData.setValue(Result.loading());
        userRepo.getUserLiveData().observeForever(dto -> {
            if (dto == null) {
                userLiveData.setValue(null);
                return;
            }

            UserUiModel uiModel = new UserUiModel(dto.uid, dto.firstName, dto.lastName, dto.username, dto.balance, dto.commissionBalance);
            userLiveData.setValue(Result.success(uiModel));
        });
    }
}