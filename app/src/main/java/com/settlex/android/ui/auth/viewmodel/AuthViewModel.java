package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.utils.AuthResultWrapper;
import com.settlex.android.data.remote.model.UserModel;
import com.settlex.android.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepo = new AuthRepository();
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResultWrapper> emailOtpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResultWrapper> emailVerifyResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResultWrapper> accountCreationResult = new MutableLiveData<>();


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
    Update uid
    ------------------------------------*/
    public void updateUid(String uid) {
        UserModel user = getOrCreateUser();
        user.setUid(uid);
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
    public void updateUserFields(String invitationCode) {
        UserModel user = userLiveData.getValue();
        if (user == null) return;

        user.setReferralCode(invitationCode.isEmpty() ? null : invitationCode);
        user.setBalance(0.00);
        user.setPasscode(null);
        user.setPasscodeSalt(null);
        user.setHasPasscode(false);
        user.setCreatedAt(null);
        user.setRole("user");

        userLiveData.setValue(user);
    }

    /*------------------------------------
    LiveData for OTP result message
    -------------------------------------*/
    public LiveData<AuthResultWrapper> getEmailOtpResult() {
        return emailOtpResult;
    }

    /*------------------------------------------
    Send OTP via AuthRepository and post result
    ------------------------------------------*/
    public void sendEmailOtp(String email) {
        authRepo.sendEmailOtp(email, new AuthRepository.SendEmailOtpCallback() {
            @Override
            public void onSuccess() {
                emailOtpResult.postValue(new AuthResultWrapper(true, "OTP Sent"));
            }

            @Override
            public void onFailure(String reason) {
                emailOtpResult.postValue(new AuthResultWrapper(false, reason));
            }
        });
    }

    /*-----------------------------------------
    LiveData for email OTP verification result
    -----------------------------------------*/
    public LiveData<AuthResultWrapper> getOtpVerifyResult() {
        return emailVerifyResult;
    }

    /*--------------------------------------------
    Verify OTP via AuthRepository and post result
    --------------------------------------------*/
    public void verifyEmailOtp(String email, String otp) {
        authRepo.verifyEmailOtp(email, otp, new AuthRepository.VerifyEmailOtpCallback() {
            @Override
            public void onSuccess() {
                emailVerifyResult.postValue(new AuthResultWrapper(true, "Verification Successful"));
            }

            @Override
            public void onFailure(String reason) {
                emailVerifyResult.postValue(new AuthResultWrapper(false, reason));
            }
        });
    }

    /*-----------------------------------------
    LiveData for User Account Creation Result
    -----------------------------------------*/
    public LiveData<AuthResultWrapper> getAccountCreationResult() {
        return accountCreationResult;
    }

    /*--------------------------------------------------
    Create Account via AuthRepository and post result
    --------------------------------------------------*/
    public void createUserAccount(UserModel user, String email, String password) {
        authRepo.createUserAccount(user, email, password, new AuthRepository.CreateAccountCallback() {
            @Override
            public void onSuccess() {
                accountCreationResult.postValue(new AuthResultWrapper(true, "Account Creation Successful"));
            }

            @Override
            public void onFailure(String reason) {
                accountCreationResult.postValue(new AuthResultWrapper(false, reason));
            }
        });
    }


}