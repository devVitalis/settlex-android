package com.settlex.android.data.exception

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException
import java.io.IOException
import javax.inject.Inject

class ApiException @Inject constructor() {

    companion object {
        private const val TAG = "ApiException"
        const val ERROR_NO_NETWORK = "Connection lost. Please check your Wi-Fi or cellular data and try again."

        // Firebase Auth
        private const val ERROR_INVALID_CREDENTIALS = "Invalid email or password."
        private const val ERROR_USER_COLLISION = "This email address is already in use."
        private const val ERROR_INVALID_USER = "This account does not exist or has been disabled."

        // Firebase Firestore
        private const val ABORTED = "The request could not be completed. Please try again."

        // Firebase Functions
        private const val NOT_FOUND = "Service unavailable. Please try again later."
        private const val INTERNAL = "A server error occurred. Please try again."
        private const val FAILED_PRECONDITION = "Your request could not be completed right now."
        private const val DEADLINE_EXCEEDED = "A server timeout occurred. Please try again."
        private const val UNAVAILABLE = "Server unavailable. Please try again later."

        private const val ERROR_FALLBACK = "Something went wrong. Please try again."
    }

    /**
     * Maps Firebase exceptions into clean domain-level exceptions.
     */
    fun map(e: Exception): AppException {
        Log.e(TAG, "Firebase error caught", e)

        return when (e) {
            is FirebaseNetworkException, is IOException -> AppException.NetworkException(ERROR_NO_NETWORK)
            is FirebaseAuthInvalidCredentialsException -> AppException.AuthException(ERROR_INVALID_CREDENTIALS)
            is FirebaseAuthUserCollisionException -> AppException.AuthException(ERROR_USER_COLLISION)
            is FirebaseAuthInvalidUserException -> AppException.AuthException(ERROR_INVALID_USER)

            is FirebaseFunctionsException -> {
                when (e.code) {
                    FirebaseFunctionsException.Code.NOT_FOUND -> AppException.ServerException(e.message ?: NOT_FOUND)
                    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> AppException.ServerException(e.message ?: DEADLINE_EXCEEDED)
                    FirebaseFunctionsException.Code.FAILED_PRECONDITION -> AppException.ServerException(e.message ?: FAILED_PRECONDITION)
                    FirebaseFunctionsException.Code.INTERNAL -> AppException.ServerException(e.message ?: INTERNAL)
                    FirebaseFunctionsException.Code.UNAVAILABLE -> AppException.ServerException(UNAVAILABLE)
                    else -> AppException.ServerException(e.message ?: ERROR_FALLBACK)
                }
            }

            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.ABORTED -> AppException.DatabaseException(ABORTED)
                    else -> AppException.ServerException(e.message ?: ERROR_FALLBACK)
                }
            }

            else -> AppException.ServerException(ERROR_FALLBACK)
        }
    }
}