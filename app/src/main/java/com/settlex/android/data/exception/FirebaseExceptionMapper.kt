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
            is FirebaseFunctionsException -> e.message ?: ERROR_FALLBACK
            else -> ERROR_FALLBACK
        }
        return Exception(message)
    }
}