package com.settlex.android.data.datasource

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.settlex.android.data.enums.TransactionServiceType
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.RecipientDto
import com.settlex.android.data.remote.dto.TransactionDto
import com.settlex.android.util.image.ImageConverter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cloudFunctions: FunctionsApiClient,
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun isPaymentIdTaken(id: String): Boolean {
        val snapshot = firestore.collection("payment_ids")
            .document(id)
            .get().await()

        return snapshot.exists()
    }

    suspend fun assignPaymentId(id: String) {
        val uid: String = getCurrentUser()?.uid ?: return

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
                mapOf(
                    "paymentId" to id,
                    "lastUpdatedAt" to FieldValue.serverTimestamp()
                ),
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

    suspend fun setProfilePhoto(context: Context, imageUri: Uri): ApiResponse<String> {
        val base64 = ImageConverter.toBase64(context, imageUri)

        return cloudFunctions.call(
            name = "api-setUserProfilePhoto",
            data = mapOf("imgBase64" to base64)
        )
    }

    suspend fun refreshUser() {
        auth.currentUser?.reload()?.await()
    }

    suspend fun transferToFriend(
        fromUid: String,
        toPaymentId: String,
        txnId: String,
        amount: Long,
        desc: String?
    ): ApiResponse<String> {
        return cloudFunctions.call(
            name = "api-transferToFriend",
            data = mapOf(
                "fromUid" to fromUid,
                "toPaymentId" to toPaymentId,
                "transactionId" to txnId,
                "amount" to amount,
                "serviceType" to TransactionServiceType.PAY_A_FRIEND,
                "description" to desc
            )
        )
    }

    suspend fun getRecipientByPaymentId(paymentId: String): ApiResponse<List<RecipientDto>> {
        return cloudFunctions.call(
            name = "api-getRecipientByPaymentId",
            data = mapOf("paymentId" to paymentId)
        )
    }

    fun getRecentTransactions(uid: String): Flow<Result<List<TransactionDto>>> {
        return callbackFlow {

            val ref = firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(2)

            val listener = ref.addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                // No snapshot or empty transactions
                if (snapshot == null || snapshot.isEmpty) {
                    trySend(Result.success(emptyList()))
                    return@addSnapshotListener
                }

                // Convert snapshot → DTOs
                val transactions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TransactionDto::class.java)
                }

                trySend(Result.success(transactions))
            }

            // Stop listening when flow is cancelled
            awaitClose { listener.remove() }
        }
    }
}