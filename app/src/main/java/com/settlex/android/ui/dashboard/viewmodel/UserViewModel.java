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

    /**
     * Update the UI model for current user details
     */
    public void fetchUserData(String uid) {
        UserUiModel cachedData = SessionManager.getInstance().getUser();
        if (cachedData != null && cachedData.getUid().equals(uid)) {
            userLiveData.setValue(cachedData);
        }

        // Fetch new data
        userRepo.getUserData(uid, userDto -> {
            if (userDto == null) {
                // If fetching new data fails, do not clear the LiveData.
                // The UI will continue to show the cached data,
                // which is better than showing an empty state on network failure.
                // You could also post an error state here if needed.
                return;
            }

            UserUiModel updatedData = new UserUiModel(
                    userDto.uid,
                    userDto.firstName,
                    userDto.lastName,
                    userDto.username,
                    userDto.balance,
                    userDto.commissionBalance);

            userLiveData.setValue(updatedData);
            SessionManager.getInstance().cacheUserData(updatedData);
        });
    }

    /**
     * Monitor user session
     */
    private void listToUserAuthState() {
        userRepo.listenToUserAuthState(user -> {
            if (user == null) {
                // If user logs out or session expires, clear the data.
                authStateLiveData.setValue(null);
                userLiveData.setValue(null);
                return;
            }
            // User is logged in
            authStateLiveData.setValue(user.getUid());
            fetchUserData(user.getUid());
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
                            StringUtil.addAtToUsername(dto.username), dto.firstName + " " + dto.lastName,
                            dto.profileUrl)); // Null on default
                }
                usernameSearchLiveData.postValue(Result.success(suggestionsUiModelList));
            }

            @Override
            public void onFailure(String reason) {
                // Post a dedicated error state.
                usernameSearchLiveData.postValue(Result.error(reason));
            }
        });
    }

    public void signOut() {
        userRepo.signOut();
    }

    @Override
    protected void onCleared() {
        Log.d("ViewModel", "Cleared");
        super.onCleared();
        userRepo.removeListener();
    }
}