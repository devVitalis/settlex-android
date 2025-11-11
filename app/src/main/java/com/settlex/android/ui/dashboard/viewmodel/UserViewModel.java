package com.settlex.android.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.utils.event.Event;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.network.NetworkMonitor;

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
    private final MediatorLiveData<Result<UserUiModel>> userLiveData = new MediatorLiveData<>();

    // Network Operation State
    private final MutableLiveData<Event<Result<String>>> uploadProfilePicLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Boolean>>> checkPaymentIdExistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> setPaymentIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> createPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Boolean>>> verifyPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> updatePasswordLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> changePaymentPinLiveData = new MutableLiveData<>();

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
            updatePasswordLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        updatePasswordLiveData.setValue(new Event<>(Result.loading()));
        userRepo.updatePassword(email, oldPassword, newPassword, new UserRepository.UpdatePasswordCallback() {
            @Override
            public void onSuccess() {
                updatePasswordLiveData.setValue(new Event<>(Result.success("Password update success")));
            }

            @Override
            public void onFailure(String error) {
                updatePasswordLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void uploadProfilePic(String imageBase64) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            uploadProfilePicLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        uploadProfilePicLiveData.setValue(new Event<>(Result.loading()));
        userRepo.uploadUserProfilePicToServer(imageBase64, new UserRepository.UploadProfilePicCallback() {
            @Override
            public void onSuccess() {
                uploadProfilePicLiveData.setValue(new Event<>(Result.success("Profile changed successful")));
            }

            @Override
            public void onFailure(String error) {
                uploadProfilePicLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void checkPaymentIdExists(String paymentId) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            checkPaymentIdExistsLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        checkPaymentIdExistsLiveData.setValue(new Event<>(Result.loading()));
        userRepo.checkPaymentIdAvailability(paymentId, new UserRepository.PaymentIdAvailableCallback() {
            @Override
            public void onSuccess(boolean exists) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(Result.success(exists)));
            }

            @Override
            public void onFailure(String error) {
                checkPaymentIdExistsLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void setPaymentId(String paymentId, String uid) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            setPaymentIdLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        setPaymentIdLiveData.setValue(new Event<>(Result.loading()));
        userRepo.storePaymentId(paymentId, uid, new UserRepository.StorePaymentIdCallback() {
            @Override
            public void onSuccess() {
                setPaymentIdLiveData.setValue(new Event<>(Result.success("success")));
            }

            @Override
            public void onFailure(String error) {
                setPaymentIdLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void createPaymentPin(String pin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            createPaymentLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        createPaymentLiveData.setValue(new Event<>(Result.loading()));
        userRepo.createPaymentPin(pin, new UserRepository.CreatePaymentPinCallback() {
            @Override
            public void onSuccess() {
                createPaymentLiveData.setValue(new Event<>(Result.success("Success")));
            }

            @Override
            public void onError(String error) {
                createPaymentLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void verifyPaymentPin(String pin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            verifyPaymentLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        verifyPaymentLiveData.setValue(new Event<>(Result.loading()));
        userRepo.validatePaymentPin(pin, new UserRepository.ValidatePaymentPinCallback() {
            @Override
            public void onSuccess(boolean isVerified) {
                verifyPaymentLiveData.setValue(new Event<>(Result.success(isVerified)));
            }

            @Override
            public void onError(String error) {
                verifyPaymentLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public void changePaymentPin(String oldPin, String newPin) {
        if (getNetworkStatus() == null || !getNetworkStatus()) {
            changePaymentPinLiveData.setValue(new Event<>(Result.noInternet()));
            return;
        }

        changePaymentPinLiveData.setValue(new Event<>(Result.loading()));
        userRepo.changePaymentPin(oldPin, newPin, new UserRepository.ChangePaymentPinCallback() {
            @Override
            public void onSuccess() {
                changePaymentPinLiveData.setValue(new Event<>(Result.success("Success")));
            }

            @Override
            public void onError(String error) {
                changePaymentPinLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    // Public API - Observers (LiveData Getters)
    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    public LiveData<Result<UserUiModel>> getUserLiveData() {
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

    public LiveData<Event<Result<String>>> getUpdatePasswordLiveData() {
        return updatePasswordLiveData;
    }

    public LiveData<Event<Result<String>>> getProfilePicUploadResult() {
        return uploadProfilePicLiveData;
    }

    public LiveData<Event<Result<Boolean>>> getPaymentIdExistsStatus() {
        return checkPaymentIdExistsLiveData;
    }

    public LiveData<Event<Result<String>>> getSetPaymentIdLiveData() {
        return setPaymentIdLiveData;
    }

    public LiveData<Event<Result<String>>> getCreatePaymentPinLiveData() {
        return createPaymentLiveData;
    }

    public LiveData<Event<Result<Boolean>>> getVerifyPaymentPinLiveData() {
        return verifyPaymentLiveData;
    }

    public LiveData<Event<Result<String>>> getChangePaymentPinLiveData() {
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

            switch (dto.getStatus()) {
                case LOADING -> userLiveData.setValue(Result.loading());
                case SUCCESS -> userLiveData.setValue(Result.success(new UserUiModel(
                        dto.getData().uid,
                        dto.getData().email,
                        dto.getData().firstName,
                        dto.getData().lastName,
                        dto.getData().createdAt,
                        dto.getData().phone,
                        dto.getData().paymentId,
                        dto.getData().photoUrl,
                        dto.getData().hasPin,
                        dto.getData().balance,
                        dto.getData().commissionBalance,
                        dto.getData().referralBalance
                )));
                case FAILURE -> userLiveData.setValue(Result.failure(dto.getError()));
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