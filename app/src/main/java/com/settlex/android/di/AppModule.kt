package com.settlex.android.di

import android.content.Context
import android.content.SharedPreferences
import com.settlex.android.data.local.PermissionPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PromoBannerPrefs

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePermissionPrefs(@ApplicationContext context: Context): PermissionPrefs {
        return PermissionPrefs(context)
    }

    @Singleton
    @PromoBannerPrefs
    @Provides
    fun providePromoBannerPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("promo_banner_prefs", Context.MODE_PRIVATE)
    }
}
