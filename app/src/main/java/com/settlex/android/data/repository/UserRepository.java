package com.settlex.android.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.SuggestionsDto;
import com.settlex.android.data.remote.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages each user account
 */
public class UserRepository {
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private ListenerRegistration userListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    // LIVEDATA HOLDER
    private final MutableLiveData<FirebaseUser> authStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserDto> userLiveData = new MutableLiveData<>();

    public UserRepository() {
        functions = FirebaseFunctions.getInstance("europe-west2");
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setAuthStateListener();
    }

    // Listen to auth changes
    private void setAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            authStateLiveData.postValue(currentUser);

            if (currentUser == null) {
                removeListener();
                userLiveData.postValue(null);
                return;
            }
            getUser(firebaseAuth.getUid());
        };
        auth.addAuthStateListener(authStateListener);
    }

    /**
     * Listens to user doc in Firestore database
     */
    public LiveData<UserDto> getUser(String uid) {
        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        userLiveData.setValue(null);
                        return;
                    }
                    UserDto userDoc = snapshot.toObject(UserDto.class);
                    userLiveData.setValue(userDoc);
                });
        return userLiveData;
    }

    /**
     * Finds matching usernames in db
     * used when user is initiating a transfer, entering userTag(Username)
     */
    public void searchUsername(String input, SearchUsernameCallback callback) {
        functions.getHttpsCallable("usernameLookup")
                .call(Collections.singletonMap("input", input))
                .addOnSuccessListener(result -> {
                    List<SuggestionsDto> suggestionsDto = new ArrayList<>();
                    Map<?, ?> data = (Map<?, ?>) result.getData();
                    if (data != null && Boolean.TRUE.equals(data.get("success"))) {
                        //noinspection unchecked
                        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");

                        if (suggestions != null) {
                            for (Map<String, Object> suggestion : suggestions) {
                                String username = (String) suggestion.get("username");
                                String firstName = (String) suggestion.get("firstName");
                                String lastName = (String) suggestion.get("lastName");
                                String profileUrl = (String) suggestion.get("profileUrl");
                                suggestionsDto.add(new SuggestionsDto(username, firstName, lastName, profileUrl));
                            }
                        }
                    }
                    callback.onResult(suggestionsDto);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure("Network request failed. Please check your network and try again");
                        return;
                    }
                    callback.onFailure(e.getMessage());
                });
    }

    // GETTERS =================
    public LiveData<FirebaseUser> getAuthState() {
        return authStateLiveData;
    }

    /**
     * Remove all Firestore listeners
     */
    public void removeListener() {
        if (userListener != null) userListener.remove();
        if (authStateListener != null) auth.removeAuthStateListener(authStateListener);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    // ============== Callbacks Interfaces
    public interface SearchUsernameCallback {
        void onResult(List<SuggestionsDto> suggestionsDto);

        void onFailure(String reason);
    }
}
