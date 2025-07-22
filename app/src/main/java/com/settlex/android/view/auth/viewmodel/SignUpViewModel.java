package com.settlex.android.view.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.model.UserModel;

// Shared ViewModel for user registration
public class SignUpViewModel extends ViewModel {

    private String plainPasscode;
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();

    public void setUser(UserModel user) {
        userLiveData.setValue(user);
    }

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public void setPlainPasscode(String pin) {
        this.plainPasscode = pin;
    }

    public String getPlainPasscode() {
        return plainPasscode;
    }
}