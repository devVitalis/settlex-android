package com.settlex.android.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T
)
