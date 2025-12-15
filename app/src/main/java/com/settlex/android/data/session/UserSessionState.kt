package com.settlex.android.data.session

import com.settlex.android.data.exception.AppException

sealed class UserSessionState<out T> {
    object Loading : UserSessionState<Nothing>()
    object UnAuthenticated : UserSessionState<Nothing>()
    data class Authenticated<T>(val user: T) : UserSessionState<T>()
    data class Error(val exception: AppException) : UserSessionState<Nothing>()
}