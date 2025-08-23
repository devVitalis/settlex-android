package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.auth.model.UserUiModel;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.util.Event;

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
    private final MutableLiveData<UserUiModel> userState = new MutableLiveData<>();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<String>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult<Boolean>> emailExistenceResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> passwordResetResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> sendEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyEmailResetOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> sendEmailVerificationOtpResult = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult<String>>> verifyEmailVerificationOtpResult = new MutableLiveData<>();

    // ====================== LIVEDATA GETTERS ======================
    public LiveData<UserModel> getUser() { return userLiveData; }
    public LiveData<AuthResult<String>> getRegisterResult() { return registerResult; }
    public LiveData<AuthResult<String>> getLoginResult() { return loginResult; }
    public LiveData<Event<AuthResult<String>>> getPasswordResetResult() { return passwordResetResult; }
    public LiveData<Event<AuthResult<String>>> getSendEmailResetOtpResult() { return sendEmailResetOtpResult; }
    public LiveData<AuthResult<Boolean>> getEmailExistenceResult() { return emailExistenceResult; }
    public LiveData<Event<AuthResult<String>>> getVerifyEmailResetOtpResult() { return verifyEmailResetOtpResult; }
    public LiveData<Event<AuthResult<String>>> getSendEmailVerificationOtpResult() { return sendEmailVerificationOtpResult; }
    public LiveData<Event<AuthResult<String>>> getVerifyEmailVerificationOtpResult() { return verifyEmailVerificationOtpResult; }
    public LiveData<UserUiModel> getUserState () {return userState; }

    /**
     * Handles user registration flow including:
     * - Profile creation
     * - Email verification initiation
     * - Error state management
     */
    public void registerUser(String email, String password, UserModel user) {
        registerResult.postValue(AuthResult.loading());
        authRepo.registerUser(user, email, password, new AuthRepository.RegisterCallback() {
            @Override public void onSuccess() { registerResult.postValue(AuthResult.success("")); }
            @Override public void onFailure(String reason) { registerResult.postValue(AuthResult.error(reason)); }
        });
    }

    /**
     * Manages email/password authentication flow with:
     * - Credential validation
     * - Session initialization
     * - Error handling for invalid attempts
     */
    public void loginWithEmail(String email, String password) {
        loginResult.postValue(AuthResult.loading());
        authRepo.loginWithEmail(email, password, new AuthRepository.LoginCallback() {
            @Override public void onSuccess() { loginResult.postValue(AuthResult.success("")); }
            @Override public void onFailure(String reason) { loginResult.postValue(AuthResult.error(reason)); }
        });
    }

    /**
     * Coordinates password reset flow:
     * 1. OTP generation
     * 2. OTP verification
     * 3. Secure password update
     */
    public void sendPasswordResetOtp(String email) {
        sendEmailResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendEmailPasswordResetOtp(email, new AuthRepository.SendOtpCallback() {
            @Override public void onSuccess() { sendEmailResetOtpResult.postValue(new Event<>(AuthResult.success("OTP Sent"))); }
            @Override public void onFailure(String reason) { sendEmailResetOtpResult.postValue(new Event<>(AuthResult.error(reason))); }
        });
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyEmailPasswordResetOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override public void onSuccess() { verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.success(""))); }
            @Override public void onFailure(String reason) { verifyEmailResetOtpResult.postValue(new Event<>(AuthResult.error(reason))); }
        });
    }

    public void requestPasswordReset(String email, String newPassword) {
        passwordResetResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.resetPassword(email, newPassword, new AuthRepository.ChangePasswordCallback() {
            @Override public void onSuccess() { passwordResetResult.postValue(new Event<>(AuthResult.success(""))); }
            @Override public void onFailure(String reason) { passwordResetResult.postValue(new Event<>(AuthResult.error(reason))); }
        });
    }

    /**
     * Manages email verification flow for new registrations
     */
    public void sendEmailVerificationOtp(String email) {
        sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.sendEmailVerificationOtp(email, new AuthRepository.SendOtpCallback() {
            @Override public void onSuccess() { sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.success(""))); }
            @Override public void onFailure(String reason) { sendEmailVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason))); }
        });
    }

    public void verifyEmailOtp(String email, String otp) {
        verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.loading()));
        authRepo.verifyEmailVerificationOtp(email, otp, new AuthRepository.VerifyOtpCallback() {
            @Override public void onSuccess() { verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.success(""))); }
            @Override public void onFailure(String reason) { verifyEmailVerificationOtpResult.postValue(new Event<>(AuthResult.error(reason))); }
        });
    }

    /**
     * Checks email uniqueness during registration
     */
    public void checkEmailExistence(String email) {
        emailExistenceResult.postValue(AuthResult.loading());
        authRepo.checkEmailExistence(email, new AuthRepository.EmailExistenceCallback() {
            @Override public void onSuccess(boolean exists) { emailExistenceResult.postValue(AuthResult.success(exists)); }
            @Override public void onFailure(String reason) { emailExistenceResult.postValue(AuthResult.error(reason)); }
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
        userState.setValue((currentUser != null) ? new UserUiModel(currentUser.getEmail(), currentUser.getDisplayName()) : null);
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