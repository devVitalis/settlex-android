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

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final MediatorLiveData<String> authStateLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Result<UserUiModel>> userLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Result<String>> uploadProfilePicLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Boolean>>> checkPaymentIdExistsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> storeUserPaymentIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> createPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Boolean>>> verifyPaymentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBalanceHiddenLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBiometricsEnabledLiveData = new MutableLiveData<>();


    // dependencies
    private final UserRepository userRepo;

    @Inject
    public UserViewModel(UserRepository userRepo) {
        this.userRepo = userRepo;

        initAuthObserver();
        initUserLiveDataObserver();

        isBiometricsEnabledLiveData.setValue(userRepo.getBiometricsEnabled());
        isBalanceHiddenLiveData.setValue(userRepo.getBalanceHidden());
    }

    // Public APIs
    public void toggleBalanceVisibility() {
        boolean isBalanceCurrentlyHidden = isBalanceHiddenLiveData.getValue() != null && isBalanceHiddenLiveData.getValue();
        boolean shouldHideBalance = !isBalanceCurrentlyHidden;

        isBalanceHiddenLiveData.setValue(shouldHideBalance);
        userRepo.toggleBalanceVisibility(shouldHideBalance);
    }

    public LiveData<Boolean> getBalanceHiddenLiveData() {
        return Transformations.distinctUntilChanged(isBalanceHiddenLiveData);
    }

    public LiveData<Boolean> getBiometricsEnabled() {
        return Transformations.distinctUntilChanged(isBiometricsEnabledLiveData);
    }

    public void setBiometricsEnabledLiveData(boolean enabled) {
        isBiometricsEnabledLiveData.setValue(enabled);
        userRepo.setBiometricsEnabled(enabled);
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
                uploadProfilePicLiveData.setValue(Result.failure(error));
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
                checkPaymentIdExistsLiveData.setValue(new Event<>(Result.failure(error)));
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
                storeUserPaymentIdLiveData.setValue(new Event<>(Result.failure(error)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getStoreUserPaymentIdStatus() {
        return storeUserPaymentIdLiveData;
    }

    public void createPaymentPin(String pin) {
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

    public LiveData<Event<Result<String>>> getCreatePaymentPinLiveData() {
        return createPaymentLiveData;
    }

    public void verifyPaymentPin(String pin) {
        verifyPaymentLiveData.setValue(new Event<>(Result.loading()));

        userRepo.VerifyPaymentPin(pin, new UserRepository.VerifyPaymentPinCallback() {
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

    public LiveData<Event<Result<Boolean>>> getVerifyPaymentPinLiveData() {
        return verifyPaymentLiveData;
    }

    /**
     * Called once during ViewModel initialization.
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

    public LiveData<Result<UserUiModel>> getUserLiveData() {
        return userLiveData;
    }
}