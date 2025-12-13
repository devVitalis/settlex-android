package com.settlex.android.di

import com.settlex.android.data.local.UserLocalDataSourceFactory
import com.settlex.android.data.local.UserLocalDataSourceFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module responsible for providing data source dependencies.
 * This module binds concrete implementations of data source factories to their interfaces,
 * allowing for dependency injection throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindUserLocalDataSourceFactory(
        factory: UserLocalDataSourceFactoryImpl
    ): UserLocalDataSourceFactory
}