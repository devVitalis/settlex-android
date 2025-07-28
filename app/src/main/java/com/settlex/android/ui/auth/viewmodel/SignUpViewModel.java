package com.settlex.android.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.model.UserModel;

public class SignUpViewModel extends ViewModel {
    private final MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();

    public void setUser(UserModel user) {
        userLiveData.setValue(user);
    }

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }
}