package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.ui.auth.model.LoginUiModel;
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.Result;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

/**
 * Manages authentication state and coordinates between UI and data layer.
 * Handles user registration, login, OTP flows, and password reset operations.
 */

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepo;

    @Inject
    public AuthViewModel(AuthRepository authRepo) {
        this.authRepo = authRepo;
        initUserAuthState();
    }

    // LIVEDATA STATE HOLDERS =========
    private final MutableLiveData<LoginUiModel> userAuthStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> emailExistenceResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> changeUserPasswordResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendPasswordResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendEmailVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyEmailVerificationOtpResult = new MutableLiveData<>();

    // LIVEDATA GETTERS ==========
    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public LiveData<Result<String>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Result<String>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Event<Result<String>>> getChangeUserPasswordResult() {
        return changeUserPasswordResult;
    }

    public LiveData<Event<Result<String>>> getSendPasswordResetOtpResult() {
        return sendPasswordResetOtpResult;
    }

    public LiveData<Result<Boolean>> getEmailExistenceResult() {
        return emailExistenceResult;
    }

    public LiveData<Event<Result<String>>> getVerifyEmailResetOtpResult() {
        return verifyEmailResetOtpResult;
    }

    public LiveData<Event<Result<String>>> getSendEmailVerificationOtpResult() {
        return sendEmailVerificationOtpResult;
    }

    public LiveData<Event<Result<String>>> getVerifyEmailVerificationOtpResult() {
        return verifyEmailVerificationOtpResult;
    }


    /**
     * Handles user registration
     */
    public void registerUser(String email, String password, UserModel user) {
        registerResult.postValue(Result.loading());
        authRepo.registerUser(user, email, password, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                registerResult.postValue(Result.success("Registration successful"));
            }

            @Override
            public void onFailure(String reason) {
                registerResult.postValue(Result.error(reason));
            }
        });
    }

    /**
     * Manages email/password login authentication
     */
    public void loginWithEmail(String email, String password) {
        loginResult.postValue(Result.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess() {
                loginResult.postValue(Result.success("Login success"));
            }

            @Override
            public void onFailure(String reason) {
                loginResult.postValue(Result.error(reason));
            }
        });
    }

    /**
     * Starts password reset flow
     */
    public void sendPasswordResetOtp(String email) {
        sendPasswordResetOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.sendEmailPasswordResetOtp(email, new AuthRepository.SendOtpCallback() {
            @Override
            public void onSuccess() {
                sendPasswordResetOtpResult.postValue(new Event<>(Result.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendPasswordResetOtpResult.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    // Send OTP
    public void verifyPasswordResetOtp(String email, String otp) {
        verifyEmailResetOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.verifyEmailPasswordResetOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override
            public void onSuccess() {
                verifyEmailResetOtpResult.postValue(new Event<>(Result.success("OTP Verified Successfully")));
            }

            @Override
            public void onFailure(String reason) {
                verifyEmailResetOtpResult.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    // Verify OTP
    public void changeUserPassword(String email, String newPassword) {
        changeUserPasswordResult.postValue(new Event<>(Result.loading()));
        authRepo.changeUserPassword(email, newPassword, new AuthRepository.ChangePasswordCallback() {
            @Override
            public void onSuccess() {
                changeUserPasswordResult.postValue(new Event<>(Result.success("Password change success")));
            }

            @Override
            public void onFailure(String reason) {
                changeUserPasswordResult.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    /**
     * Starts email verification flow for new registrations
     */
    public void sendEmailVerificationOtp(String email) {
        sendEmailVerificationOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.sendEmailVerificationOtp(email, new AuthRepository.SendOtpCallback() {
            @Override
            public void onSuccess() {
                sendEmailVerificationOtpResult.postValue(new Event<>(Result.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendEmailVerificationOtpResult.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    // Verify OTP
    public void verifyEmailOtp(String email, String otp) {
        verifyEmailVerificationOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.verifyEmailVerificationOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override
            public void onSuccess() {
                verifyEmailVerificationOtpResult.postValue(new Event<>(Result.success("OTP Verified Successfully")));
            }

            @Override
            public void onFailure(String reason) {
                verifyEmailVerificationOtpResult.postValue(new Event<>(Result.error(reason)));
            }
        });
    }

    /**
     * Checks email uniqueness during registration
     */
    public void checkEmailExistence(String email) { // Not implemented
        emailExistenceResult.postValue(Result.loading());
        authRepo.checkEmailExistence(email, new AuthRepository.EmailExistenceCallback() {
            @Override
            public void onSuccess(boolean exists) {
                emailExistenceResult.postValue(Result.success(exists));
            }

            @Override
            public void onFailure(String reason) {
                emailExistenceResult.postValue(Result.error(reason));
            }
        });
    }

    // USER STATE MUTATORS =========
    public void updateFirstName(String firstName) {
        UserModel user = getOrCreateUser();
        user.setFirstName(firstName);
        userLiveData.setValue(user);
    }

    public void updateLastName(String lastName) {
        UserModel user = getOrCreateUser();
        user.setLastName(lastName);
        userLiveData.setValue(user);
    }

    public void updateEmail(String email) {
        UserModel user = getOrCreateUser();
        user.setEmail(email);
        userLiveData.setValue(user);
    }

    public void updatePhone(String phone) {
        UserModel user = getOrCreateUser();
        user.setPhone(phone);
        userLiveData.setValue(user);
    }

    public String getEmail() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user.getEmail() : null;
    }

    public void initUserAuthState() {
        FirebaseUser currentUser = authRepo.getCurrentUser();
        if (currentUser == null) userAuthStateLiveData.setValue(null);

        userAuthStateLiveData.setValue((currentUser != null) ? new LoginUiModel(
                currentUser.getUid(),
                currentUser.getEmail(),
                currentUser.getDisplayName(),
                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null)
                : null);
    }

    public void signOut() {
        // Log out current user / end session
        authRepo.signOut();
    }

    public LiveData<LoginUiModel> getUserAuthStateLiveData() {
        return userAuthStateLiveData;
    }

    public boolean isUserLoggedIn() {
        return authRepo.getCurrentUser() != null;
    }

    /**
     * Prepares user model for registration by:
     * - Setting referral code if available and pin default values fields
     */
    public void applyDefaultUserValues(String invitationCode) {
        UserModel user = getOrCreateUser();
        user.setReferralCode(!invitationCode.isEmpty() ? invitationCode : null);
        user.setPin(null);
        user.setPinSalt(null);
        user.setHasPin(false);
        userLiveData.setValue(user);
    }

    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user : new UserModel();
    }
}