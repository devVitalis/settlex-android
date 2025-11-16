package com.settlex.android.data.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.remote.api.MetadataService
import com.settlex.android.data.remote.dto.BackendResponseDto
import com.settlex.android.data.remote.dto.MetadataDto
import com.settlex.android.domain.model.UserModel
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val firebaseMessaging: FirebaseMessaging,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {

    companion object {
        private val TAG = AuthRemoteDataSource::class.java.simpleName
    }

    fun signOut() = auth.signOut()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // AUTH
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(user: UserModel, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
        val createdUser = authResult.user ?: throw Exception("Registration failed")

        val finalUser = user.copy(uid = createdUser.uid)

        createProfile(finalUser)
        setDisplayName("${finalUser.firstName} ${finalUser.lastName}")
    }

    private suspend fun createProfile(user: UserModel) {
        val data = mapOf("user" to gson.toJson(user))
        functions.getHttpsCallable("api-createProfile").call(data).await()
    }

    private suspend fun setDisplayName(fullName: String) {
        getCurrentUser()?.let { user ->
            try {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update user display name", e)
            }
        }
    }

    // CLOUD FUNCTIONS
    suspend fun sendOtp(email: String, type: OtpType): BackendResponseDto {
        return runCloudFunction(
            "api-sendOtp",
            mapOf("email" to email, "type" to type.name)
        )
    }

    suspend fun verifyEmail(email: String, otp: String): BackendResponseDto {
        return runCloudFunction(
            "api-verifyEmail",
            mapOf("email" to email, "otp" to otp)
        )
    }

    suspend fun verifyPasswordReset(email: String, otp: String): BackendResponseDto {
        return runCloudFunction(
            "api-verifyPasswordReset",
            mapOf("email" to email, "otp" to otp)
        )
    }

    suspend fun setNewPassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): BackendResponseDto {

        val metadata = collectMetadata()

        return runCloudFunction(
            "api-setNewPassword",
            mapOf(
                "email" to email,
                "oldPassword" to oldPassword,
                "newPassword" to newPassword,
                "metadata" to gson.toJson(metadata)
            )
        )
    }

    // METADATA
    suspend fun collectMetadata(): MetadataDto {
        return suspendCancellableCoroutine { cont ->
            MetadataService.collectMetadata { md ->
                cont.resume(md)
            }
        }
    }

    // FCM
    suspend fun getFcmToken(): String {
        return firebaseMessaging.token.await()
    }

    suspend fun storeFcmToken(token: String) {
        getCurrentUser()?.let { user ->
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .update("fcmToken", token)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to store FCM token", e)
            }
        }
    }

    // FUNCTION WRAPPER
    private suspend fun runCloudFunction(name: String, data: Map<String, Any?>): BackendResponseDto {
        val response = functions.getHttpsCallable(name)
            .call(data)
            .await()

        val json = gson.toJson(response.data)

        return gson.fromJson(json, BackendResponseDto::class.java)
    }
}
