package com.settlex.android.data.datasource

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.settlex.android.data.datasource.utils.CloudFunctions
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.util.image.ImageConverter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cloudFunctions: CloudFunctions
) {

    suspend fun isPaymentIdTaken(id: String): Boolean {
        val snapshot = firestore.collection("payment_ids")
            .document(id)
            .get().await()

        return snapshot.exists()
    }

    suspend fun assignPaymentId(id: String, uid: String) {
        firestore.runTransaction { transaction ->
            val globalDocRef = firestore.collection("payment_ids").document(id)
            val userDocRef = firestore.collection("users").document(uid)

            // Check if the payment ID is already taken
            val snapshot = transaction.get(globalDocRef)
            if (snapshot.exists()) {
                throw FirebaseFirestoreException(
                    "Payment ID is already taken",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            transaction.set(globalDocRef, mapOf("uid" to uid))
            transaction.set(
                userDocRef,
                mapOf("paymentId" to id),
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun setPaymentPin(pin: String): ApiResponse<String> {
        return cloudFunctions.call(
            name = "api-setPaymentPin",
            data = mapOf("pin" to pin)
        )
    }

    suspend fun authPaymentPin(pin: String): ApiResponse<Boolean> {
        return cloudFunctions.call(
            name = "api-authPaymentPin",
            data = mapOf("pin" to pin)
        )
    }

    suspend fun resetPaymentPin(oldPin: String, newPin: String): ApiResponse<String> {
        return cloudFunctions.call(
            name = "api-resetPaymentPin",
            data = mapOf("oldPin" to oldPin, "newPin" to newPin)
        )
    }

    suspend fun resetPassword(oldPwd: String, newPwd: String) {
        val user = auth.currentUser!!
        val authCredential = EmailAuthProvider.getCredential(user.email!!, oldPwd)

        user.reauthenticate(authCredential).await()
        user.updatePassword(newPwd).await()
    }

    suspend fun setProfilePicture(context: Context, uri: Uri): ApiResponse<String> {
        val base64 = ImageConverter.toBase64(context, uri)

        return cloudFunctions.call(
            name = "api-setProfilePicture",
            data = mapOf("imgBase64" to base64)
        )
    }

    suspend fun refreshUser() {
        auth.currentUser?.reload()?.await()
    }
}