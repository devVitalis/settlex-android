package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;
import com.settlex.android.ui.auth.util.AuthResult;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepo = new AuthRepository();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> emailOtpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> emailVerifyResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> signUpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> signInResult = new MutableLiveData<>();


    /*------------------------------------
    Safely get or create UserModel
    ------------------------------------*/
    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        if (user == null) {
            user = new UserModel();
        }
        return user;
    }

    /*------------------------------------
    Public LiveData getter
    ------------------------------------*/
    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    /*------------------------------------
    Update first name
    ------------------------------------*/
    public void updateFirstName(String firstName) {
        UserModel user = getOrCreateUser();
        user.setFirstName(firstName);
        userLiveData.setValue(user);
    }

    /*------------------------------------
    Update last name
    ------------------------------------*/
    public void updateLastName(String lastName) {
        UserModel user = getOrCreateUser();
        user.setLastName(lastName);
        userLiveData.setValue(user);
    }

    /*------------------------------------
    Update email
    ------------------------------------*/
    public void updateEmail(String email) {
        UserModel user = getOrCreateUser();
        user.setEmail(email);
        userLiveData.setValue(user);
    }

    /*------------------------------------
    Get current email (may return null)
    ------------------------------------*/
    public String getEmail() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user.getEmail() : null;
    }

    /*------------------------------------
    Update phone number
    ------------------------------------*/
    public void updatePhone(String phone) {
        UserModel user = getOrCreateUser();
        user.setPhone(phone);
        userLiveData.setValue(user);
    }

    /*------------------------------------
    Update: Finalize user data
    ------------------------------------*/
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

    /*------------------------------------
    Getters (LiveData) to NOTIFY UIs
    -------------------------------------*/
    // getEmailOtpResult
    public LiveData<AuthResult> getEmailOtpResult() {
        return emailOtpResult;
    }

    // getOtpVerifyResult
    public LiveData<AuthResult> getOtpVerifyResult() {
        return emailVerifyResult;
    }

    // getSignUpResult
    public LiveData<AuthResult> getSignUpResult() {
        return signUpResult;
    }

    // getSignInResult
    public LiveData<AuthResult> getSignInResult() {
        return signInResult;
    }

    /*------------------------------------------
    Send OTP via AuthRepository and post result
    ------------------------------------------*/
    public void sendEmailOtp(String email) {
        authRepo.sendEmailOtp(email, new AuthRepository.SendEmailOtpCallback() {
            @Override
            public void onSuccess() {
                emailOtpResult.postValue(new AuthResult(true, "OTP Sent"));
            }

            @Override
            public void onFailure(String reason) {
                emailOtpResult.postValue(new AuthResult(false, reason));
            }
        });
    }

    /*--------------------------------------------
    Verify OTP via AuthRepository and post result
    --------------------------------------------*/
    public void verifyEmailOtp(String email, String otp) {
        authRepo.verifyEmailOtp(email, otp, new AuthRepository.VerifyEmailOtpCallback() {
            @Override
            public void onSuccess() {
                emailVerifyResult.postValue(new AuthResult(true, "Verification Successful"));
            }

            @Override
            public void onFailure(String reason) {
                emailVerifyResult.postValue(new AuthResult(false, reason));
            }
        });
    }

    /*--------------------------------------------------
    Create Account via AuthRepository and post result
    --------------------------------------------------*/
    public void signUpUser(UserModel user, String email, String password) {
        authRepo.signUpWithEmailAndPassword(user, email, password, new AuthRepository.CreateAccountCallback() {
            @Override
            public void onSuccess() {
                signUpResult.postValue(new AuthResult(true, ""));
            }

            @Override
            public void onFailure(String reason) {
                signUpResult.postValue(new AuthResult(false, reason));
            }
        });
    }

    /*------------------------------------------------------
    SignIn user and POST result to UI (Email && Password)
    ------------------------------------------------------*/
    public void signInUser(String email, String password) {
        authRepo.signInWithEmail(email, password, new AuthRepository.SignInCallback() {
            @Override
            public void onSuccess() {
                signInResult.postValue(new AuthResult(true, ""));
            }

            @Override
            public void onFailure(String reason) {
                signInResult.postValue(new AuthResult(false, reason));
            }
        });
    }
}