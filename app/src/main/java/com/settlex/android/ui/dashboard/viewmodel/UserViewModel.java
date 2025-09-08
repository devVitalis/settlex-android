package com.settlex.android.ui.dashboard.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.settlex.android.data.local.SessionManager;
import com.settlex.android.data.remote.dto.SuggestionsDto;
import com.settlex.android.data.repository.UserRepository;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepo;

    // LiveData holders
    private final MutableLiveData<UserUiModel> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> authStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<List<RecipientUiModel>>> usernameSearchLiveData = new MutableLiveData<>();


    public UserViewModel() {
        this.userRepo = new UserRepository();
        listToUserAuthState();
    }

    //  GETTERS ============
    public LiveData<String> getAuthStateLiveData() {
        return authStateLiveData;
    }

    public LiveData<Result<List<RecipientUiModel>>> getUsernameSearchLiveData() {
        return usernameSearchLiveData;
    }

    public LiveData<UserUiModel> getUserData() {
        return userLiveData;
    }

    public UserUiModel getCacheUserData(){
        return SessionManager.getInstance().getUser();
    }

    /**
     * Update the UI model for current user details
     */
    public void fetchUserData(String uid) {
        if (userLiveData.getValue() != null) return;
        Log.d("ViewModel", "Fetching user data");
        userRepo.getUserData(uid, userDto -> {
            if (userDto == null) {
                userLiveData.setValue(null);
                return;
            }
            userLiveData.setValue(new UserUiModel(
                    userDto.uid,
                    userDto.firstName,
                    userDto.lastName,
                    userDto.username,
                    userDto.balance,
                    userDto.commissionBalance
            ));
            SessionManager.getInstance().cacheUserData(userLiveData.getValue());
        });
    }

    /**
     * Monitor user session
     */
    private void listToUserAuthState() {
        Log.d("ViewModel", "Listening to auth state");
        userRepo.listenToUserAuthState(user -> {
            if (user == null) {
                authStateLiveData.setValue(null);
                return;
            }
            fetchUserData(user.getUid());
            authStateLiveData.setValue(user.getUid());
        });
    }

    /**
     * Returns username query results.
     */
    public void searchUsername(String query) {
        usernameSearchLiveData.postValue(Result.loading());
        userRepo.searchUsername(query, new UserRepository.SearchUsernameCallback() {
            @Override
            public void onResult(List<SuggestionsDto> suggestionsDto) {
                // Map DTO -> UI Model
                List<RecipientUiModel> suggestionsUiModelList = new ArrayList<>();
                for (SuggestionsDto dto : suggestionsDto) {
                    suggestionsUiModelList.add(new RecipientUiModel(
                            StringUtil.addAtToUsername(dto.username),
                            dto.firstName + " " + dto.lastName,
                            dto.profileUrl));
                }
                usernameSearchLiveData.postValue(Result.success(suggestionsUiModelList));
            }

            @Override
            public void onFailure(String reason) {
                usernameSearchLiveData.postValue(Result.success(Collections.emptyList()));
            }
        });
    }

    public void signOut() {
        userRepo.signOut();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepo.removeListener();
    }
}
