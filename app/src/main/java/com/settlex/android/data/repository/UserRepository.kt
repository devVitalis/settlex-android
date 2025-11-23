package com.settlex.android.data.repository

import android.content.Context
import android.net.Uri
import com.settlex.android.data.remote.dto.ApiResponse

interface UserRepository {
    suspend fun isPaymentIdTaken(id: String): Result<Boolean>
    suspend fun assignPaymentId(id: String, uid: String): Result<Unit>
    suspend fun resetPassword(oldPwd: String, newPwd: String): Result<Unit>
    suspend fun setPaymentPin(pin: String): Result<ApiResponse<String>>
    suspend fun authPaymentPin(pin: String): Result<ApiResponse<Boolean>>
    suspend fun resetPaymentPin(oldPin: String, newPin: String): Result<ApiResponse<String>>
    suspend fun setProfilePicture(context: Context, uri: Uri): Result<ApiResponse<String>>
    suspend fun refreshUser()
}