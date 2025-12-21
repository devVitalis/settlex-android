package com.settlex.android.domain.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.remote.dto.RecipientDto

interface UserRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun isPaymentIdTaken(id: String): Result<Boolean>
    suspend fun assignPaymentId(id: String): Result<Unit>
    suspend fun resetPassword(oldPwd: String, newPwd: String): Result<Unit>
    suspend fun setPaymentPin(pin: String): Result<ApiResponse<String>>
    suspend fun authPaymentPin(pin: String): Result<ApiResponse<Boolean>>
    suspend fun resetPaymentPin(oldPin: String, newPin: String): Result<ApiResponse<String>>
    suspend fun setProfilePhoto(context: Context, imageUri: Uri): Result<ApiResponse<String>>
    suspend fun refreshUser()
    suspend fun getRecipientByPaymentId(paymentId: String): Result<ApiResponse<List<RecipientDto>>>
}