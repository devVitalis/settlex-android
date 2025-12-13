package com.settlex.android.data.session

import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.dto.UserDto

sealed class UserSessionState {
    object Loading : UserSessionState()
    object LoggedOut : UserSessionState()
    data class LoggedIn(val user: UserDto) : UserSessionState()
    data class Error(val exception: AppException) : UserSessionState()
}