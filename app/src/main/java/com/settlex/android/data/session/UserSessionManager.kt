package com.settlex.android.data.session

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.settlex.android.SettleXApp
import com.settlex.android.data.datasource.UserLocalDataSource
import com.settlex.android.data.remote.dto.UserDto
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    private var _userLocalDataSource: UserLocalDataSource? = null
    val userLocalDataSource: UserLocalDataSource
        get() = _userLocalDataSource
            ?: throw IllegalStateException("UserLocalDataSource requested before login")

    companion object {
        private const val TAG = "UserSessionManager"
    }

    private val _authState = MutableStateFlow<FirebaseUser?>(null)
    val authState = _authState.asStateFlow()

    private val _userState = MutableStateFlow<UserDto?>(null)
    val userState = _userState.asStateFlow()

    private var userListener: ListenerRegistration? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        initAuthStateListener()
    }

    private fun initAuthStateListener() {
        Log.d(TAG, "Initializing AuthStateListener.")

        if (authListener != null) return

        authListener = FirebaseAuth.AuthStateListener { fbAuth ->
            val user = fbAuth.currentUser
            _authState.value = user

            if (user != null) {
                attachUserListener(user.uid)

                _userLocalDataSource = UserLocalDataSource(
                    SettleXApp.appContext,
                    user.uid
                )

            } else {
                detachUserListener()
                _userState.value = null
                _userLocalDataSource = null
            }
        }

        auth.addAuthStateListener(authListener!!)
    }

    private fun attachUserListener(uid: String) {
        Log.d(TAG, "Attaching a new user listener for user: $uid")

        userListener?.remove()

        userListener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _userState.value = null
                    Log.e(TAG, "User snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot !== null && snapshot.exists()) {
                    _userState.value = snapshot.toObject(UserDto::class.java)
                }
            }
    }

    private fun detachUserListener() {
        userListener?.remove()
        userListener = null
    }
}
