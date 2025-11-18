package com.settlex.android.data.exception

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.functions.FirebaseFunctionsException
import java.io.IOException
import javax.inject.Inject

class FirebaseExceptionMapper @Inject constructor() {

    companion object {
        private const val TAG = "FirebaseExceptionMapper"
        private const val ERROR_FALLBACK = "Something went wrong. Please try again."
    }

    /**
     * Maps Firebase exceptions into clean domain-level exceptions.
     */
    fun map(e: Exception): Exception {
        Log.e(TAG, "Firebase error caught", e)

        val message = when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            is FirebaseAuthUserCollisionException -> "This email address is already in use."
            is FirebaseAuthInvalidUserException -> "This account does not exist or has been disabled."
            is FirebaseNetworkException, is IOException -> "Connection lost. Please check your Wi-Fi or cellular data and try again."
            is FirebaseFunctionsException -> {
                when (e.code) {
                    FirebaseFunctionsException.Code.NOT_FOUND -> "Service unavailable. Please try again later."
                    FirebaseFunctionsException.Code.INTERNAL -> "A server error occurred. Please try again."
                    FirebaseFunctionsException.Code.FAILED_PRECONDITION -> "Your request could be completed right now."
                    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> "A server timeout occurred. Please try again."
                    else -> e.message ?: ERROR_FALLBACK
                }
            }

            else -> e.message ?: ERROR_FALLBACK
        }
        return Exception(message)
    }
}