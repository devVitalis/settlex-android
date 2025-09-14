package com.settlex.android.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.functions.FirebaseFunctions;
import com.settlex.android.data.remote.dto.UserDto;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Manages each user account
 */
@Singleton
public class UserRepository {
    private final FirebaseFunctions functions;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private ListenerRegistration userListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    private final MutableLiveData<UserDto> userLiveData = new MutableLiveData<>();    // Shared Livedata

    @Inject
    public UserRepository(FirebaseAuth auth, FirebaseFirestore firestore, FirebaseFunctions functions) {
        this.functions = functions;
        this.firestore = firestore;
        this.auth = auth;
    }

    // Listen to auth changes
    public void listenToUserAuthState(UserAuthStateCallback callback) {
        if (authStateListener != null) return;

        authStateListener = firebaseAuth -> callback.onResult(firebaseAuth.getCurrentUser());
        auth.addAuthStateListener(authStateListener);
    }

    /**
     * Attaches the Firestore listener once per user session.
     */
    public void setupUserListener(String uid) {
        if (userListener != null) {
            // Listener is already active, no need to re-attach
            return;
        }
        Log.d("Repository", "attaching new listener .....");
        userListener = firestore.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        userLiveData.setValue(null);
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        userLiveData.setValue(null);
                        return;
                    }
                    userLiveData.setValue((snapshot.toObject(UserDto.class)));
                });
    }

    /**
     * Exposes the shared LiveData instance.
     * All ViewModels will observe this same object.
     */
    public LiveData<UserDto> getUserLiveData() {
        return userLiveData;
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        removeListeners();
    }

    public void removeListeners() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
            authStateListener = null;
        }
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    // Callbacks Interfaces
    public interface UserAuthStateCallback {
        void onResult(FirebaseUser user);
    }
}