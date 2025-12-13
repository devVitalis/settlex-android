package com.settlex.android.data.local

import com.settlex.android.data.datasource.UserLocalDataSource

interface UserLocalDataSourceFactory {
    fun create(uid: String): UserLocalDataSource
}