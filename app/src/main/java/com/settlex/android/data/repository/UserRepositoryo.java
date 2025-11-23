package com.settlex.android.data.repository;

import com.settlex.android.data.local.UserPrefs;

import jakarta.inject.Singleton;

@Singleton
public class UserRepositoryo {

    private UserPrefs getUserPrefs() {
//        return new UserPrefs(this, "");
    }

    public boolean getPayBiometricsEnabled() {
        return getUserPrefs().isPayBiometricsEnabled();
    }

    public void setPayBiometricsEnabled(boolean enable) {
        getUserPrefs().setPayBiometricsEnabled(enable);
    }

    public boolean getLoginBiometricsEnabled() {
        return getUserPrefs().isLoginBiometricsEnabled();
    }

    public void setLoginBiometricsEnabled(boolean enable) {
        getUserPrefs().setLoginBiometricsEnabled(enable);
    }

    public boolean getBalanceHidden() {
        return getUserPrefs().isBalanceHidden();
    }

    public void toggleBalanceVisibility(boolean shouldHideBalance) {
        getUserPrefs().setBalanceHidden(shouldHideBalance);
    }

}
