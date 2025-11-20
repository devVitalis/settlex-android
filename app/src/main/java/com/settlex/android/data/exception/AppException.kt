package com.settlex.android.data.exception

sealed class AppException : Exception() {
    data class NetworkException(override val message: String) : AppException()
    data class AuthException(override val message: String) : AppException()
    data class ServerException(override val message: String) : AppException()
}