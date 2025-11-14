package com.settlex.android.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.settlex.android.domain.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.auth.login.LoginUiModel;
import com.settlex.android.util.event.Event;
import com.settlex.android.util.event.Result;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

/**
 * ViewModel responsible for managing all authentication flows and temporary user state during onboarding.
 */
@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final MutableLiveData<UserModel> sharedUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<LoginUiModel> currentUserLiveData = new MutableLiveData<>();

    private final MutableLiveData<Result<String>> loginLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> createAccountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> fcmTokenLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> checkEmailExistsLiveData = new MutableLiveData<>();

    // One shot events
    private final MutableLiveData<Event<Result<String>>> setNewPasswordLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendPasswordResetCodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyPasswordResetLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendVerificationCodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyEmailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoginBiometricsEnabledLiveData = new MutableLiveData<>();

    // dependencies
    private final AuthRepository authRepo;
    private final UserRepository userRepo;

    @Inject
    public AuthViewModel(AuthRepository authRepo, UserRepository userRepo) {
        this.authRepo = authRepo;
        this.userRepo = userRepo;

        // Sync Firebase user on startup
        loadCurrentUserState();
    }

    // user prefs
    public LiveData<Boolean> getLoginBiometricsEnabled() {
        isLoginBiometricsEnabledLiveData.setValue(userRepo.getLoginBiometricsEnabled());
        return isLoginBiometricsEnabledLiveData;
    }

    /**
     * Creates a new account
     */
    public void createAccount(String email, String password, UserModel user) {
        createAccountLiveData.postValue(Result.loading());
        authRepo.createAccount(user, email, password, new AuthRepository.CreateAccountCallback() {
            @Override
            public void onSuccess() {
                createAccountLiveData.postValue(Result.success("Registration successful"));
            }

            @Override
            public void onFailure(String reason) {
                createAccountLiveData.postValue(Result.failure(reason));
            }
        });
    }

    public LiveData<Result<String>> getCreateAccountLiveData() {
        return createAccountLiveData;
    }

    /**
     * Login via email & password
     */
    public void loginWithEmail(String email, String password) {
        loginLiveData.postValue(Result.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess() {
                loginLiveData.postValue(Result.success("Login success"));
            }

            @Override
            public void onFailure(String reason) {
                loginLiveData.postValue(Result.failure(reason));
            }
        });
    }

    public LiveData<Result<String>> getLoginLiveData() {
        return loginLiveData;
    }

    /**
     * Fetch FCM token for device
     */
    public LiveData<Result<String>> getFcmToken() {
        fcmTokenLiveData.setValue(Result.loading());
        authRepo.getFcmToken(new AuthRepository.FcmTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                fcmTokenLiveData.setValue(Result.success(token));
            }

            @Override
            public void onTokenError() {
                fcmTokenLiveData.setValue(Result.failure("Failed to get FCM token"));
            }
        });
        return fcmTokenLiveData;
    }

    // Password Reset
    public void sendPasswordResetCode(String email) {
        sendPasswordResetCodeLiveData.postValue(new Event<>(Result.loading()));
        authRepo.sendPasswordResetCode(email, new AuthRepository.SendVerificationCodeCallback() {
            @Override
            public void onSuccess() {
                sendPasswordResetCodeLiveData.postValue(new Event<>(Result.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendPasswordResetCodeLiveData.postValue(new Event<>(Result.failure(reason)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getSendPasswordResetCodeLiveData() {
        return sendPasswordResetCodeLiveData;
    }

    public void verifyPasswordReset(String email, String otp) {
        verifyPasswordResetLiveData.postValue(new Event<>(Result.loading()));
        authRepo.verifyPasswordReset(email, otp, new AuthRepository.VerifyPasswordCallback() {
            @Override
            public void onSuccess() {
                verifyPasswordResetLiveData.postValue(new Event<>(Result.success("OTP Verified Successfully")));
            }

            @Override
            public void onFailure(String reason) {
                verifyPasswordResetLiveData.postValue(new Event<>(Result.failure(reason)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getVerifyPasswordResetLiveData() {
        return verifyPasswordResetLiveData;
    }

    public void setNewPassword(String email, String newPassword) {
        setNewPasswordLiveData.postValue(new Event<>(Result.loading()));
        authRepo.setNewPassword(email, newPassword, new AuthRepository.SetNewPasswordCallback() {
            @Override
            public void onSuccess() {
                setNewPasswordLiveData.postValue(new Event<>(Result.success("Password changed successfully")));
            }

            @Override
            public void onFailure(String reason) {
                setNewPasswordLiveData.postValue(new Event<>(Result.failure(reason)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getSetNewPasswordLiveData() {
        return setNewPasswordLiveData;
    }

    // Email Verification
    public void sendVerificationCode(String email) {
        sendVerificationCodeLiveData.postValue(new Event<>(Result.loading()));
        authRepo.sendVerificationCode(email, new AuthRepository.SendVerificationCodeCallback() {
            @Override
            public void onSuccess() {
                sendVerificationCodeLiveData.postValue(new Event<>(Result.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendVerificationCodeLiveData.postValue(new Event<>(Result.failure(reason)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getSendVerificationCodeLiveData() {
        return sendVerificationCodeLiveData;
    }

    public void verifyEmail(String email, String otp) {
        verifyEmailLiveData.postValue(new Event<>(Result.loading()));
        authRepo.verifyEmail(email, otp, new AuthRepository.VerifyEmailCallback() {
            @Override
            public void onSuccess() {
                verifyEmailLiveData.postValue(new Event<>(Result.success("OTP Verified Successfully")));
            }

            @Override
            public void onFailure(String reason) {
                verifyEmailLiveData.postValue(new Event<>(Result.failure(reason)));
            }
        });
    }

    public LiveData<Event<Result<String>>> getVerifyEmailLiveData() {
        return verifyEmailLiveData;
    }

    /**
     * Check if email exists during signup flow
     */
    public void checkEmailExists(String email) {
        checkEmailExistsLiveData.postValue(Result.loading());
        authRepo.checkEmailExists(email, new AuthRepository.CheckEmailExistsCallback() {
            @Override
            public void onSuccess(boolean exists) {
                checkEmailExistsLiveData.postValue(Result.success(exists));
            }

            @Override
            public void onFailure(String reason) {
                checkEmailExistsLiveData.postValue(Result.failure(reason));
            }
        });
    }

    public LiveData<Result<Boolean>> getCheckEmailExistsLiveData() {
        return checkEmailExistsLiveData;
    }


    // Local User State (registration progress)
    public void updateFirstName(String firstName) {
        UserModel user = getOrCreateUser();
        user.setFirstName(firstName);
        sharedUserLiveData.setValue(user);
    }

    public void updateLastName(String lastName) {
        UserModel user = getOrCreateUser();
        user.setLastName(lastName);
        sharedUserLiveData.setValue(user);
    }

    public void updateEmail(String email) {
        UserModel user = getOrCreateUser();
        user.setEmail(email);
        sharedUserLiveData.setValue(user);
    }

    public void updatePhone(String phone) {
        UserModel user = getOrCreateUser();
        user.setPhone(phone);
        sharedUserLiveData.setValue(user);
    }

    public void updateFcmToken(String token) {
        UserModel user = getOrCreateUser();
        user.setFcmToken(token);
        sharedUserLiveData.setValue(user);
    }

    public String getEmail() {
        UserModel user = sharedUserLiveData.getValue();
        return (user != null) ? user.getEmail() : null;
    }

    public LiveData<UserModel> getUser() {
        return sharedUserLiveData;
    }

    /**
     * Apply initial default values during registration
     */
    public void applyDefaultUserValues(String invitationCode) {
        UserModel user = getOrCreateUser();
        user.setReferralCode(!invitationCode.isEmpty() ? invitationCode : null);
        user.setHasPin(false);
        sharedUserLiveData.setValue(user);
    }

    private UserModel getOrCreateUser() {
        UserModel existing = sharedUserLiveData.getValue();
        return (existing != null) ? existing : new UserModel();
    }

    /**
     * Sync ViewModel with currently logged-in Firebase user
     */
    private void loadCurrentUserState() {
        FirebaseUser user = authRepo.getCurrentUser();

        if (user != null) {
            currentUserLiveData.setValue(new LoginUiModel(
                    user.getUid(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null
            ));
        }
    }

    public LiveData<LoginUiModel> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    // Session Controls
    public void signOut() {
        authRepo.signOut();
    }

    public boolean isUserLoggedIn() {
        return authRepo.getCurrentUser() != null;
    }
}
