package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.settlex.android.domain.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.auth.model.AuthUserUiModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.event.Event;

/**
 * Manages authentication state and coordinates between UI and data layer.
 * Handles user registration, login, OTP flows, and password reset operations.
 */
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepo;

    public AuthViewModel (){
        authRepo = new AuthRepository();
        updateUserState();
    }

    // ====================== LIVEDATA STATE HOLDERS ======================
    private final MutableLiveData<AuthUserUiModel> userStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> emailExistenceResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> passwordResetResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> sendEmailVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<String>>> verifyEmailVerificationOtpResult = new MutableLiveData<>();

    // ====================== LIVEDATA GETTERS ======================
    public LiveData<UserModel> getUser() { return userLiveData; }
    public LiveData<Result<String>> getRegisterResult() { return registerResult; }
    public LiveData<Result<String>> getLoginResult() { return loginResult; }
    public LiveData<Event<Result<String>>> getPasswordResetResult() { return passwordResetResult; }
    public LiveData<Event<Result<String>>> getSendEmailResetOtpResult() { return sendEmailResetOtpResult; }
    public LiveData<Result<Boolean>> getEmailExistenceResult() { return emailExistenceResult; }
    public LiveData<Event<Result<String>>> getVerifyEmailResetOtpResult() { return verifyEmailResetOtpResult; }
    public LiveData<Event<Result<String>>> getSendEmailVerificationOtpResult() { return sendEmailVerificationOtpResult; }
    public LiveData<Event<Result<String>>> getVerifyEmailVerificationOtpResult() { return verifyEmailVerificationOtpResult; }
    public LiveData<AuthUserUiModel> getUserStateLiveData() {return userStateLiveData; }

    /**
     * Handles user registration flow including:
     * - Profile creation
     * - Email verification initiation
     * - Error state management
     */
    public void registerUser(String email, String password, UserModel user) {
        registerResult.postValue(Result.loading());
        authRepo.registerUser(user, email, password, new AuthRepository.RegisterCallback() {
            @Override public void onSuccess() { registerResult.postValue(Result.success("")); }
            @Override public void onFailure(String reason) { registerResult.postValue(Result.error(reason)); }
        });
    }

    /**
     * Manages email/password authentication flow with:
     * - Credential validation
     * - Session initialization
     * - Error handling for invalid attempts
     */
    public void loginWithEmail(String email, String password) {
        loginResult.postValue(Result.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginCallback() {
            @Override public void onSuccess() { loginResult.postValue(Result.success("")); }
            @Override public void onFailure(String reason) { loginResult.postValue(Result.error(reason)); }
        });
    }

    /**
     * Coordinates password reset flow:
     * 1. OTP generation
     * 2. OTP verification
     * 3. Secure password update
     */
    public void sendPasswordResetOtp(String email) {
        sendEmailResetOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.sendEmailPasswordResetOtp(email, new AuthRepository.SendOtpCallback() {
            @Override public void onSuccess() { sendEmailResetOtpResult.postValue(new Event<>(Result.success("OTP Sent"))); }
            @Override public void onFailure(String reason) { sendEmailResetOtpResult.postValue(new Event<>(Result.error(reason))); }
        });
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        verifyEmailResetOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.verifyEmailPasswordResetOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override public void onSuccess() { verifyEmailResetOtpResult.postValue(new Event<>(Result.success(""))); }
            @Override public void onFailure(String reason) { verifyEmailResetOtpResult.postValue(new Event<>(Result.error(reason))); }
        });
    }

    public void requestPasswordReset(String email, String newPassword) {
        passwordResetResult.postValue(new Event<>(Result.loading()));
        authRepo.resetPassword(email, newPassword, new AuthRepository.ChangePasswordCallback() {
            @Override public void onSuccess() { passwordResetResult.postValue(new Event<>(Result.success(""))); }
            @Override public void onFailure(String reason) { passwordResetResult.postValue(new Event<>(Result.error(reason))); }
        });
    }

    /**
     * Manages email verification flow for new registrations
     */
    public void sendEmailVerificationOtp(String email) {
        sendEmailVerificationOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.sendEmailVerificationOtp(email, new AuthRepository.SendOtpCallback() {
            @Override public void onSuccess() { sendEmailVerificationOtpResult.postValue(new Event<>(Result.success(""))); }
            @Override public void onFailure(String reason) { sendEmailVerificationOtpResult.postValue(new Event<>(Result.error(reason))); }
        });
    }

    public void verifyEmailOtp(String email, String otp) {
        verifyEmailVerificationOtpResult.postValue(new Event<>(Result.loading()));
        authRepo.verifyEmailVerificationOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override public void onSuccess() { verifyEmailVerificationOtpResult.postValue(new Event<>(Result.success(""))); }
            @Override public void onFailure(String reason) { verifyEmailVerificationOtpResult.postValue(new Event<>(Result.error(reason))); }
        });
    }

    /**
     * Checks email uniqueness during registration
     */
    public void checkEmailExistence(String email) {
        emailExistenceResult.postValue(Result.loading());
        authRepo.checkEmailExistence(email, new AuthRepository.EmailExistenceCallback() {
            @Override public void onSuccess(boolean exists) { emailExistenceResult.postValue(Result.success(exists)); }
            @Override public void onFailure(String reason) { emailExistenceResult.postValue(Result.error(reason)); }
        });
    }

    // ====================== USER STATE MUTATORS ======================
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

    public void updateUserState() {
        FirebaseUser currentUser = authRepo.getCurrentUser();
        userStateLiveData.setValue((currentUser != null) ? new AuthUserUiModel(
                currentUser.getUid(),
                currentUser.getEmail(),
                currentUser.getDisplayName()) : null);
    }

    /**
     * Prepares user model for registration by:
     * - Setting referral code if available
     * - Initializing security fields
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

    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user : new UserModel();
    }
}