package com.settlex.android.data.remote.dto

import android.util.Log

data class ApiResponse<T>(
    val success: Boolean,
    val data: T
){
   init {
       Log.d("ApiResponse", "Success: $success, Data: $data")
   }
}
