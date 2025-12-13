package com.settlex.android.data.local

import android.content.Context
import com.settlex.android.data.datasource.UserLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class UserLocalDataSourceFactoryImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    UserLocalDataSourceFactory {
    override fun create(uid: String): UserLocalDataSource {
        return UserLocalDataSource(context = context, uid = uid)
    }
}