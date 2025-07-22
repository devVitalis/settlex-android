package com.settlex.android.manager;

import android.telephony.ims.RegistrationManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.settlex.android.data.model.UserModel;

import java.util.Objects;

public class AuthManager {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Initialize Firebase Instances in the constructor
    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /*-----------------------------------------------------------------
    Create user account in FirebaseAuth using email/password.
    Called during onboarding before storing user profile in Firestore.
    -----------------------------------------------------------------*/
    public void createUserAccount(UserModel user, String email, String password, CreateUserAccountCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get and set user unique ID
                        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        user.setUid(uid);
                        saveUserToDatabase(user, callback);
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException){
                            callback.onFailure("This email is already registered. Kindly log in");
                        } else {
                            callback.onFailure("Registration failed, try again!");
                        }
                    }
                });
    }

    /*----------------------------------
    Create user profile in Firestore db
    ----------------------------------*/
    private void saveUserToDatabase(UserModel user, CreateUserAccountCallback callback){
        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess());
    }

    /*---------------------------------------
    Callback Interfaces For Success/Failures
    ---------------------------------------*/
    public interface CreateUserAccountCallback {
        void onSuccess();

        void onFailure(String reason);
    }
}