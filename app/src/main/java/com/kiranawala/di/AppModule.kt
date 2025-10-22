package com.kiranawala.di

import android.app.Application
import android.content.Context
import com.kiranawala.data.local.encrypted_storage.EncryptedStorageManager
import com.kiranawala.data.local.preferences.PreferencesManager
import com.kiranawala.utils.network.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext context: Context): Application {
        return context.applicationContext as Application
    }
    
    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Singleton
    @Provides
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)
    
    @Singleton
    @Provides
    fun provideEncryptedStorageManager(
        @ApplicationContext context: Context
    ): EncryptedStorageManager = EncryptedStorageManager(context)
    
    @Singleton
    @Provides
    fun provideNetworkConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager = NetworkConnectivityManager(context)
}