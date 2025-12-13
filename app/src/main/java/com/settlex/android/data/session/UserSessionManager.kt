package com.settlex.android.data.session

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.settlex.android.data.datasource.UserLocalDataSource
import com.settlex.android.data.exception.ApiException
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.local.UserLocalDataSourceFactory
import com.settlex.android.data.remote.dto.UserDto
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class UserSessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataSourceFactory: UserLocalDataSourceFactory,
    private val apiException: ApiException,
    applicationScope: CoroutineScope
) {
    private var _userLocalDataSource: UserLocalDataSource? = null

    val userLocalDataSource: UserLocalDataSource
        get() = _userLocalDataSource
            ?: throw IllegalStateException("User data source not initialized")

    private val authState: Flow<FirebaseUser?> = callbackFlow {
        Log.d(TAG, "Initializing auth state listener")
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        auth.addAuthStateListener(listener)

         awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSessionState: StateFlow<UserSessionState> = authState
        .flatMapLatest { user ->
            when (user) {
                null -> handleLoggedOut()
                else -> handleLoggedIn(user.uid)
            }
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSessionState.Loading
        )

    private fun handleLoggedOut(): Flow<UserSessionState> {
        Log.d(TAG, "Removing _userLocalDataSource on Logged out")
        _userLocalDataSource = null
        return flowOf(UserSessionState.LoggedOut)
    }

    private fun handleLoggedIn(uid: String): Flow<UserSessionState> {
        Log.d(TAG, "Attaching a new user listener for $uid")

        _userLocalDataSource = dataSourceFactory.create(uid)

        return firestore.collection("users")
            .document(uid)
            .dataObjects<UserDto>()
            .map { userDto ->
                userDto?.let { UserSessionState.LoggedIn(it) }
                    ?: UserSessionState.Error(
                        AppException.DatabaseException(
                            "User profile not found."
                        )
                    )
            }
            .catch { exception ->
                Log.e(TAG, "Error fetching user profile", exception)
                emit(UserSessionState.Error(apiException.map(exception as Exception)))
            }
    }

    companion object {
        private val TAG = UserSessionManager::class.simpleName
    }
}