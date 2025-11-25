package com.settlex.android.domain.usecase.user

import android.content.Context
import android.net.Uri
import com.settlex.android.data.remote.dto.ApiResponse
import com.settlex.android.data.repository.UserRepositoryImpl
import jakarta.inject.Inject

class SetProfilePictureUseCase @Inject constructor(private val userRepoImpl: UserRepositoryImpl) {
    suspend operator fun invoke(context: Context, uri: Uri): Result<ApiResponse<String>> {
        return userRepoImpl.setProfilePicture(context, uri)
    }
}