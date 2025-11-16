package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.UiState;
import com.settlex.android.util.network.NetworkMonitor;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {

    // --------------------------------------------------------------------------------
    // Dependencies
    // --------------------------------------------------------------------------------
    private final UserRepository userRepo;

    // --------------------------------------------------------------------------------
    // LiveData State
    // --------------------------------------------------------------------------------

    // User & Auth State (Mediators)
    private final MediatorLiveData<String> authStateLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<UiState<UserUiModel>> userLiveData = new MediatorLiveData<>();

    // Network Operation State
    private final MutableLiveData<Event<UiState<String>>> uploadProfilePicLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<Boolean>>> checkPaymentIdExistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<String>>> setPaymentIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<String>>> createPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<Boolean>>> verifyPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<String>>> updatePasswordLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<UiState<String>>> changePaymentPinLiveData = new MutableLiveData<>();

    // Local UI Preferences State
    private final MutableLiveData<Boolean> isBalanceHiddenLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPayBiometricsEnabledLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoginBiometricsEnabledLiveData = new MutableLiveData<>();


    @Inject
    public UserViewModel(UserRepository userRepo) {
        this.userRepo = userRepo;

        // Initialize observers
        initAuthObserver();
        initUserLiveDataObserver();

        // Load initial state from repository (preferences)
//        isBalanceHiddenLiveData.setValue(userRepo.getBalanceHidden());
//        isPayBiometricsEnabledLiveData.setValue(userRepo.getPayBiometricsEnabled());
//        isLoginBiometricsEnabledLiveData.setValue(userRepo.getLoginBiometricsEnabled());
    }

    // Public API - Actions (Called by UI)
    public void signOut() {
        userRepo.signOut();
    }

    // --- Local Preferences ---

    public void toggleBalanceVisibility() {
        boolean isBalanceCurrentlyHidden = isBalanceHiddenLiveData.getValue() != null && isBalanceHiddenLiveData.getValue();
        boolean shouldHideBalance = !isBalanceCurrentlyHidden;

        isBalanceHiddenLiveData.setValue(shouldHideBalance);
        userRepo.toggleBalanceVisibility(shouldHideBalance);
    }

    public void setPayBiometricsEnabledLiveData(boolean enabled) {
        isPayBiometricsEnabledLiveData.setValue(enabled);
        userRepo.setPayBiometricsEnabled(enabled);
    }

    public void setLoginBiometricsEnabledLiveData(boolean enabled) {
        isLoginBiometricsEnabledLiveData.setValue(enabled);
        userRepo.setLoginBiometricsEnabled(enabled);
    }

    // --- Network Operations ---
    public void updatePassword(String email, String oldPassword, String newPassword) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            updatePasswordLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        updatePasswordLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.updatePassword(email, oldPassword, newPassword, new UserRepository.UpdatePasswordCallback() {
            @Override
            public void onSuccess() {
                updatePasswordLiveData.setValue(new Event<>(UiState.success("Password update success")));
            }

            @Override
            public void onFailure(String error) {
                updatePasswordLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void uploadProfilePic(String imageBase64) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            uploadProfilePicLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        uploadProfilePicLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.uploadUserProfilePicToServer(imageBase64, new UserRepository.UploadProfilePicCallback() {
            @Override
            public void onSuccess() {
                uploadProfilePicLiveData.setValue(new Event<>(UiState.success("Profile changed successful")));
            }

            @Override
            public void onFailure(String error) {
                uploadProfilePicLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void checkPaymentIdExists(String paymentId) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            checkPaymentIdExistsLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        checkPaymentIdExistsLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.checkPaymentIdAvailability(paymentId, new UserRepository.PaymentIdAvailableCallback() {
            @Override
            public void onSuccess(boolean exists) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(UiState.success(exists)));
            }

            @Override
            public void onFailure(String error) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void setPaymentId(String paymentId, String uid) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            setPaymentIdLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        setPaymentIdLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.storePaymentId(paymentId, uid, new UserRepository.StorePaymentIdCallback() {
            @Override
            public void onSuccess() {
                setPaymentIdLiveData.setValue(new Event<>(UiState.success("success")));
            }

            @Override
            public void onFailure(String error) {
                setPaymentIdLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void createPaymentPin(String pin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            createPaymentLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        createPaymentLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.createPaymentPin(pin, new UserRepository.CreatePaymentPinCallback() {
            @Override
            public void onSuccess() {
                createPaymentLiveData.setValue(new Event<>(UiState.success("Success")));
            }

            @Override
            public void onError(String error) {
                createPaymentLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void verifyPaymentPin(String pin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            verifyPaymentLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        verifyPaymentLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.validatePaymentPin(pin, new UserRepository.ValidatePaymentPinCallback() {
            @Override
            public void onSuccess(boolean isVerified) {
                verifyPaymentLiveData.setValue(new Event<>(UiState.success(isVerified)));
            }

            @Override
            public void onError(String error) {
                verifyPaymentLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    public void changePaymentPin(String oldPin, String newPin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            changePaymentPinLiveData.setValue(new Event<>(UiState.noInternet()));
            return;
        }

        changePaymentPinLiveData.setValue(new Event<>(UiState.loading()));
        userRepo.changePaymentPin(oldPin, newPin, new UserRepository.ChangePaymentPinCallback() {
            @Override
            public void onSuccess() {
                changePaymentPinLiveData.setValue(new Event<>(UiState.success("Success")));
            }

            @Override
            public void onError(String error) {
                changePaymentPinLiveData.setValue(new Event<>(UiState.failure(error)));
            }
        });
    }

    // Public API - Observers (LiveData Getters)
    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    public LiveData<UiState<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getBalanceHiddenLiveData() {
        return Transformations.distinctUntilChanged(isBalanceHiddenLiveData);
    }

    public LiveData<Boolean> getPayBiometricsEnabled() {
        return Transformations.distinctUntilChanged(isPayBiometricsEnabledLiveData);
    }

    public LiveData<Boolean> getLoginBiometricsEnabled() {
        return Transformations.distinctUntilChanged(isLoginBiometricsEnabledLiveData);
    }

    public LiveData<Event<UiState<String>>> getUpdatePasswordLiveData() {
        return updatePasswordLiveData;
    }

    public LiveData<Event<UiState<String>>> getProfilePicUploadResult() {
        return uploadProfilePicLiveData;
    }

    public LiveData<Event<UiState<Boolean>>> getPaymentIdExistsStatus() {
        return checkPaymentIdExistsLiveData;
    }

    public LiveData<Event<UiState<String>>> getSetPaymentIdLiveData() {
        return setPaymentIdLiveData;
    }

    public LiveData<Event<UiState<String>>> getCreatePaymentPinLiveData() {
        return createPaymentLiveData;
    }

    public LiveData<Event<UiState<Boolean>>> getVerifyPaymentPinLiveData() {
        return verifyPaymentLiveData;
    }

    public LiveData<Event<UiState<String>>> getChangePaymentPinLiveData() {
        return changePaymentPinLiveData;
    }

    // Private Helpers
    /**
     * Called once during ViewModel initialization.
     * Observes the repository's auth state.
     */
    private void initAuthObserver() {
        authStateLiveData.addSource(userRepo.getSharedUserAuthState(), user -> {
            if (user == null) {
                // Logged out
                authStateLiveData.setValue(null);
                // transactionLiveData.setValue(Result.success(Collections.emptyList()));
                return;
            }
            // Logged in -> set UID
            authStateLiveData.setValue(user.getUid());
        });
    }

    /**
     * Called. once during ViewModel initialization.
     * Observes the repository's user data and maps it to a UI model.
     */
    private void initUserLiveDataObserver() {
        userLiveData.addSource(userRepo.getSharedUserLiveData(), dto -> {
            if (dto == null) {
                return;
            }

            switch (dto.status) {
                case LOADING -> userLiveData.setValue(UiState.loading());
                case SUCCESS -> userLiveData.setValue(UiState.success(new UserUiModel(
                        dto.data.uid,
                        dto.data.email,
                        dto.data.firstName,
                        dto.data.lastName,
                        dto.data.createdAt,
                        dto.data.phone,
                        dto.data.paymentId,
                        dto.data.photoUrl,
                        dto.data.hasPin,
                        dto.data.balance,
                        dto.data.commissionBalance,
                        dto.data.referralBalance
                )));
                case FAILURE -> userLiveData.setValue(UiState.failure(dto.getError()));
            }
        });
    }

    /**
     * Helper to get the current network status.
     */
    private Boolean getNetworkStatus() {
        return NetworkMonitor.getNetworkStatus().getValue();
    }
}