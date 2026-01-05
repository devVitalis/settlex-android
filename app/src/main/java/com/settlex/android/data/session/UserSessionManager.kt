package com.settlex.android.data.session

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.settlex.android.data.datasource.UserLocalDataSource
import com.settlex.android.data.exception.ExceptionMapper
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.local.UserLocalDataSourceFactory
import com.settlex.android.data.remote.dto.UserDto
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Singleton
class UserSessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataSourceFactory: UserLocalDataSourceFactory,
    private val exceptionMapper: ExceptionMapper,
    private val applicationScope: CoroutineScope
) {
    private var profileJob: Job? = null
    private var _userLocalDataSource: UserLocalDataSource? = null

    /**
     * Provides access to the local data source for the currently logged-in user.
     * This property is lazily initialized upon successful user authentication and becomes null on logout.
     *
     * @throws IllegalStateException if accessed before a user is logged in (Initialized).
     */
    val userLocalDataSource: UserLocalDataSource
        get() = _userLocalDataSource
            ?: throw IllegalStateException("User data source not initialized")

    private val _userSession = MutableStateFlow<UserSessionState<UserDto>>(UserSessionState.Loading)
    val userSession = _userSession.asStateFlow()

    init {
        Log.d(TAG, "$TAG initialized")
        initAuthStateListener()
    }

    private fun initAuthStateListener() = auth.addAuthStateListener { auth ->
        when (auth.currentUser) {
            null -> clearUserSession()
            else -> setupUserSession(auth.currentUser!!.uid)
        }
    }

    private fun clearUserSession() {
        // Cancel any running profile fetch
        profileJob?.cancel()
        _userLocalDataSource = null
        _userSession.value = UserSessionState.UnAuthenticated
    }

    private fun setupUserSession(uid: String) {
        _userSession.value = UserSessionState.Loading

        // Initialize UserLocalDataSource
        _userLocalDataSource = dataSourceFactory.create(uid)

        // Fetch user profile
        profileJob = firestore.collection("users")
            .document(uid)
            .dataObjects<UserDto>()
            .onEach { userDto ->
                if (userDto != null) {
                    _userSession.value = UserSessionState.Authenticated(userDto)
                } else {
                    // Document doesn't exist yet or deleted
                    _userSession.value = UserSessionState.Error(
                        AppException.DatabaseException("User profile not found.")
                    )
                }
            }
            .catch { throwable ->
                when (throwable) {
                    is Exception -> _userSession.value = UserSessionState.Error(
                        exceptionMapper.map(throwable)
                    )
                    else -> throw throwable
                }
            }
            .launchIn(applicationScope)
    }

    companion object {
        private val TAG = UserSessionManager::class.simpleName
    }
}