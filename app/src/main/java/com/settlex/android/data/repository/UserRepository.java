package com.settlex.android.data.repository;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.RecipientDto;
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

    public UserRepository() {
        functions = FirebaseFunctions.getInstance("europe-west2");
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Listen to auth changes
    public void listenToUserAuthState(GetUserAuthStateCallback callback) {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            callback.onResult(user);
        };
        auth.addAuthStateListener(authStateListener);
    }

    /**
     * Listens to user doc in Firestore database
     */
    public void getUserData(String uid, GetUserCallback callback) {
        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()){
                        callback.onResult(null);
                        return;
                    }

                    UserDto userDoc = snapshot.toObject(UserDto.class);
                    callback.onResult(userDoc);
                });
    }

    /**
     * Finds matching usernames in db
     * used when user is initiating a transfer, entering userTag(Username)
     */
    public void searchUsername(String input, SearchUsernameCallback callback) {
        functions.getHttpsCallable("searchUsername")
                .call(Collections.singletonMap("input", input))
                .addOnSuccessListener(result -> {
                    List<RecipientDto> recipientDto = new ArrayList<>();
                    Map<?, ?> data = (Map<?, ?>) result.getData();
                    if (data != null && Boolean.TRUE.equals(data.get("success"))) {
                        //noinspection unchecked
                        List<Map<String, Object>> recipientDtos = (List<Map<String, Object>>) data.get("suggestions");

                        if (recipientDtos != null) {
                            for (Map<String, Object> recipient : recipientDtos) {
                                String username = (String) recipient.get("username");
                                String firstName = (String) recipient.get("firstName");
                                String lastName = (String) recipient.get("lastName");
                                String profileUrl = (String) recipient.get("profileUrl");
                                recipientDto.add(new RecipientDto(username, firstName, lastName, profileUrl));
                            }
                        }
                    }
                    callback.onResult(recipientDto);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseNetworkException || e instanceof IOException) {
                        callback.onFailure("Network request failed. Please check your network and try again");
                        return;
                    }
                    callback.onFailure(e.getMessage());
                });
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
        void onResult(List<RecipientDto> suggestionsDto);

        void onFailure(String reason);
    }

    public interface GetUserCallback {
        void onResult(UserDto userDto);
        void onError(String error);
    }

    public interface GetUserAuthStateCallback {
        void onResult(FirebaseUser user);
    }
}
