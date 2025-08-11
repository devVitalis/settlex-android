package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.util.Event;

/**
 * ViewModel for handling authentication-related operations.
 * Acts as the bridge between UI and AuthRepository.
 */
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepo = new AuthRepository();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> sendResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<Boolean>> emailExistenceResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> sendVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyVerificationOtpResult = new MutableLiveData<>();

    // -------------------- Public Getters --------------------

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public LiveData<AuthResult<String>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<AuthResult<String>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Event<AuthResult<String>>> getSendResetOtpResult() {
        return sendResetOtpResult;
    }

    public LiveData<AuthResult<Boolean>> getEmailExistenceResult() {
        return emailExistenceResult;
    }

    public LiveData<Event<AuthResult<String>>> getVerifyResetOtpResult() {
        return verifyResetOtpResult;
    }

    public LiveData<Event<AuthResult<String>>> getSendVerificationOtpResult() {
        return sendVerificationOtpResult;
    }

    public LiveData<Event<AuthResult<String>>> getVerifyVerificationOtpResult() {
        return verifyVerificationOtpResult;
    }

    // -------------------- Auth Actions --------------------

    public void registerUser(String email, String password, UserModel user) {
        registerResult.postValue(AuthResult.loading());
        authRepo.registerUser(user, email, password, new AuthRepository.RegisterUserCallback() {
            @Override
            public void onSuccess() {
                registerResult.postValue(AuthResult.success("Success"));
            }

            @Override
            public void onFailure(String reason) {
                registerResult.postValue(AuthResult.error(reason));
            }
        });
    }

    public void loginWithEmail(String email, String password) {
        loginResult.postValue(AuthResult.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginWithEmailCallback() {
            @Override
            public void onSuccess() {
                loginResult.postValue(AuthResult.success("Success"));
            }

            @Override
            public void onFailure(String reason) {
                loginResult.postValue(AuthResult.error(reason));
            }
        });
    }

    public void sendPasswordResetOtp(String email) {
        sendResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendPasswordResetOtp(email, new AuthRepository.SendPasswordResetOtpCallback() {
            @Override
            public void onSuccess() {
                sendResetOtpResult.postValue(new Event<>(AuthResult.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendResetOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        verifyResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyPasswordResetOtp(email, otp, new AuthRepository.VerifyPasswordResetOtpCallback() {
            @Override
            public void onSuccess() {
                verifyResetOtpResult.postValue(new Event<>(AuthResult.success("Verification Successful")));
            }

            @Override
            public void onFailure(String reason) {
                verifyResetOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    public void sendEmailVerificationOtp(String email) {
        sendVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendEmailVerificationOtp(email, new AuthRepository.SendEmailVerificationOtpCallback() {
            @Override
            public void onSuccess() {
                sendVerificationOtpResult.postValue(new Event<>(AuthResult.success("OTP Sent")));
            }

            @Override
            public void onFailure(String reason) {
                sendVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    public void verifyEmailVerificationOtp(String email, String otp) {
        verifyVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyEmailVerificationOtp(email, otp, new AuthRepository.VerifyEmailVerificationOtpCallback() {
            @Override
            public void onSuccess() {
                verifyVerificationOtpResult.postValue(new Event<>(AuthResult.success("Verification Successful")));
            }

            @Override
            public void onFailure(String reason) {
                verifyVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason)));
            }
        });
    }

    public void checkEmailExistence(String email) {
        authRepo.checkEmailExistence(email, new AuthRepository.CheckEmailExistenceCallback() {
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

    // -------------------- User Updates --------------------

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

    public void finalizeUserFields(String invitationCode) {
        UserModel user = userLiveData.getValue();
        if (user == null) return;
        user.setReferralCode(invitationCode.isEmpty() ? null : invitationCode);
        user.setBalance(0.00);
        user.setPasscode(null);
        user.setPasscodeSalt(null);
        user.setHasPasscode(false);
        user.setRole("user");
        user.setCreatedAt(null);
        userLiveData.setValue(user);
    }

    // -------------------- Private Helpers --------------------

    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user : new UserModel();
    }
}