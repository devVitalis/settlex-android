package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.util.Event;

/**
 * Manages UI-related data and business logic for the authentication feature.
 * This ViewModel coordinates interactions between the UI controllers and the AuthRepository,
 * exposing observable data streams via LiveData.
 */
public class AuthViewModel extends ViewModel {
    // Note: For improved testability and scalability, AuthRepository should be
    // provided through dependency injection rather than direct instantiation.
    private final AuthRepository authRepo = new AuthRepository();

    // --- LiveData State Holders ---
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<Boolean>> emailExistenceResult = new MutableLiveData<>();

    /**
     * Emits results for single-fire events, such as navigation or toast messages.
     * The Event wrapper prevents these from being re-observed on configuration changes.
     */
    private final MutableLiveData<Event<AuthResult<String>>> sendEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> sendEmailVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyEmailVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> passwordResetResult = new MutableLiveData<>();

    // -------------------- Public LiveData Accessors --------------------

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public LiveData<AuthResult<String>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<AuthResult<String>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Event<AuthResult<String>>> getPasswordResetResult() {
        return passwordResetResult;
    }

    public LiveData<Event<AuthResult<String>>> getSendEmailResetOtpResult() {
        return sendEmailResetOtpResult;
    }

    public LiveData<AuthResult<Boolean>> getEmailExistenceResult() {
        return emailExistenceResult;
    }

    public LiveData<Event<AuthResult<String>>> getVerifyEmailResetOtpResult() {
        return verifyEmailResetOtpResult;
    }

    public LiveData<Event<AuthResult<String>>> getSendEmailVerificationOtpResult() {
        return sendEmailVerificationOtpResult;
    }

    public LiveData<Event<AuthResult<String>>> getVerifyEmailVerificationOtpResult() {
        return verifyEmailVerificationOtpResult;
    }

    // -------------------- Asynchronous Actions ---------------------

    /**
     * Triggers the user registration process.
     * The operation's outcome is posted to {@link #registerResult}.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param user     The completed {@link UserModel} with all required details.
     */
    public void registerUser(String email, String password, UserModel user) {
        registerResult.postValue(AuthResult.loading());
        authRepo.registerUser(user, email, password, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                registerResult.postValue(AuthResult.success(""));
            }

            @Override
            public void onFailure(String reason) {
                registerResult.postValue(AuthResult.error(reason));
            }
        });
    }

    /**
     * Triggers the email and password login process.
     * The operation's outcome is posted to {@link #loginResult}.
     */
    public void loginWithEmail(String email, String password) {
        loginResult.postValue(AuthResult.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess() {
                loginResult.postValue(AuthResult.success(""));
            }

            @Override
            public void onFailure(String reason) {
                loginResult.postValue(AuthResult.error(reason));
            }
        });
    }

    /**
     * Requests a password reset OTP for the given email.
     * The operation's outcome is posted to {@link #sendEmailResetOtpResult}.
     */
    public void sendPasswordResetOtp(String email) {
        sendEmailResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendEmailPasswordResetOtp(email, new AuthRepository.SendOtpCallback() {
            @Override
            public void onSuccess() {
                sendEmailResetOtpResult.postValue(new Event<>(AuthResult.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendEmailResetOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    /**
     * Verifies a password reset OTP.
     * The operation's outcome is posted to {@link #verifyEmailResetOtpResult}.
     */
    public void verifyPasswordResetOtp(String email, String otp) {
        verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyEmailPasswordResetOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override
            public void onSuccess() {
                verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.success("")));
            }

            @Override
            public void onFailure(String reason) {
                verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    /**
     * Requests a password change for the given email along with new password.
     * The operation's outcome is posted to {@link #passwordResetResult}.
     */
    public void requestPasswordReset(String email, String newPassword){
        passwordResetResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.resetPassword(email, newPassword, new AuthRepository.ChangePasswordCallback() {
            @Override
            public void onSuccess() {
                passwordResetResult.postValue(new Event<>(AuthResult.success("")));
            }

            @Override
            public void onFailure(String reason) {
                passwordResetResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    /**
     * Sends an OTP for verifying a new user's email address.
     * The operation's outcome is posted to {@link #sendEmailVerificationOtpResult}.
     */
    public void sendEmailVerificationOtp(String email) {
        sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendEmailVerificationOtp(email, new AuthRepository.SendOtpCallback() {
            @Override
            public void onSuccess() {
                sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.success("")));
            }

            @Override
            public void onFailure(String reason) {
                sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    /**
     * Verifies an email verification OTP.
     * The operation's outcome is posted to {@link #verifyEmailVerificationOtpResult}.
     */
    public void verifyEmailOtp(String email, String otp) {
        verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyEmailVerificationOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override
            public void onSuccess() {
                verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.success("")));
            }

            @Override
            public void onFailure(String reason) {
                verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    /**
     * Checks if an email is already associated with an existing account.
     * The result is posted to {@link #emailExistenceResult}.
     */
    public void checkEmailExistence(String email) {
        emailExistenceResult.postValue(AuthResult.loading());
        authRepo.checkEmailExistence(email, new AuthRepository.EmailExistenceCallback() {
            @Override
            public void onSuccess(boolean exists) {
                emailExistenceResult.postValue(AuthResult.success(exists));
            }

            @Override
            public void onFailure(String reason) {
                emailExistenceResult.postValue(AuthResult.error(reason));
            }
        });
    }

    // -------------------- User State Mutators --------------------

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

    /**
     * Populates the user model with default values before the final registration call.
     * This prepares the object for serialization and persistence.
     *
     * @param invitationCode An optional referral code.
     */
    public void applyDefaultUserValues(String invitationCode) {
        UserModel user = userLiveData.getValue();
        if (user == null) return;
        user.setReferralCode(!invitationCode.isEmpty() ? invitationCode : null);
        user.setPin(null);
        user.setPinSalt(null);
        user.setHasPin(false);
        userLiveData.setValue(user);
    }

    // -------------------- Private Helpers --------------------

    /**
     * Ensures a non-null {@link UserModel} instance is available for state mutation.
     * If the current LiveData value is null, a new instance is created.
     *
     * @return The existing or a new {@link UserModel} instance.
     */
    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user : new UserModel();
    }
}