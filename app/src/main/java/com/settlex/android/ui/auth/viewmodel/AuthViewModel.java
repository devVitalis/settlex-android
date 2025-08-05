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
    private final MutableLiveData<AuthResult> sendEmailOtpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> emailVerifyResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> signUpResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> signInResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> checkEmailExistResult = new MutableLiveData<>();

    /*------------------------------------
    Safely retrieve or initialize user
    ------------------------------------*/
    private UserModel getOrCreateUser() {
        UserModel user = userLiveData.getValue();
        if (user == null) {
            user = new UserModel();
        }
        return user;
    }

    /*-------------------------------
    Expose LiveData for user model
    -------------------------------*/
    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    /*-------------------------
    Update user's first name
    -------------------------*/
    public void updateFirstName(String firstName) {
        UserModel user = getOrCreateUser();
        user.setFirstName(firstName);
        userLiveData.setValue(user);
    }

    /*------------------------
    Update user's last name
    ------------------------*/
    public void updateLastName(String lastName) {
        UserModel user = getOrCreateUser();
        user.setLastName(lastName);
        userLiveData.setValue(user);
    }

    /*---------------------
    Update user's email
    ---------------------*/
    public void updateEmail(String email) {
        UserModel user = getOrCreateUser();
        user.setEmail(email);
        userLiveData.setValue(user);
    }

    /*---------------------------------
    Retrieve email (may return null)
    ---------------------------------*/
    public String getEmail() {
        UserModel user = userLiveData.getValue();
        return (user != null) ? user.getEmail() : null;
    }

    /*-----------------------------
    Update user's phone number
    -----------------------------*/
    public void updatePhone(String phone) {
        UserModel user = getOrCreateUser();
        user.setPhone(phone);
        userLiveData.setValue(user);
    }

    /*---------------------------------------------
    Finalize user fields before account creation
    ---------------------------------------------*/
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

    /*------------------------------------------------
    Expose LiveData results to be observed by the UI
    ------------------------------------------------*/
    // Email OTP result
    public LiveData<AuthResult> getSendEmailOtpResult() {
        return sendEmailOtpResult;
    }

    // Email OTP verification result
    public LiveData<AuthResult> getOtpVerifyResult() {
        return emailVerifyResult;
    }

    // User sign-up result
    public LiveData<AuthResult> getSignUpResult() {
        return signUpResult;
    }

    // User sign-in result
    public LiveData<AuthResult> getSignInResult() {
        return signInResult;
    }

    // Email existence check result
    public LiveData<AuthResult> getCheckExistsResult() {
        return checkEmailExistResult;
    }

    /*------------------------------------------------
    Send email OTP via repository and post result
    ------------------------------------------------*/
    public void sendEmailOtp(String email) {
        authRepo.sendEmailOtp(email, new AuthRepository.SendEmailOtpCallback() {
            @Override
            public void onSuccess() {
                sendEmailOtpResult.postValue(AuthResult.success("OTP Sent"));
            }

            @Override
            public void onFailure(String reason) {
                sendEmailOtpResult.postValue(AuthResult.failure(reason));
            }
        });
    }

    /*-----------------------------------------------------
    Verify email OTP via repository and post result
    -----------------------------------------------------*/
    public void verifyEmailOtp(String email, String otp) {
        authRepo.verifyEmailOtp(email, otp, new AuthRepository.VerifyEmailOtpCallback() {
            @Override
            public void onSuccess() {
                emailVerifyResult.postValue(AuthResult.success("Verification Successful"));
            }

            @Override
            public void onFailure(String reason) {
                emailVerifyResult.postValue(AuthResult.failure(reason));
            }
        });
    }

    /*-----------------------------------------------------
    Register new user via repository and post result
    -----------------------------------------------------*/
    public void signUpUser(UserModel user, String email, String password) {
        authRepo.signUpWithEmailAndPassword(user, email, password, new AuthRepository.CreateAccountCallback() {
            @Override
            public void onSuccess() {
                signUpResult.postValue(AuthResult.success("success"));
            }

            @Override
            public void onFailure(String reason) {
                signUpResult.postValue(AuthResult.failure("Sign up failed. Please try again."));
            }
        });
    }

    /*-----------------------------------------------
    Sign in user via repository and post result
    -----------------------------------------------*/
    public void signInUser(String email, String password) {
        authRepo.signInWithEmail(email, password, new AuthRepository.SignInCallback() {
            @Override
            public void onSuccess() {
                signInResult.postValue(AuthResult.success("success"));
            }

            @Override
            public void onFailure(String reason) {
                signInResult.postValue(AuthResult.failure(reason));
            }
        });
    }

    /*---------------------------------------------------
    Check if email exists in Firebase and post result
    ---------------------------------------------------*/
    public void checkEmailExists(String email) {
        authRepo.checkEmailExist(email, new AuthRepository.CheckEmailExistCallback() {
            @Override
            public void onSuccess(boolean exists) {
                checkEmailExistResult.postValue(AuthResult.exists(exists));
            }

            @Override
            public void onFailure(String reason) {
                checkEmailExistResult.postValue(AuthResult.failure(reason));
            }
        });
    }

}