package com.kiranawala.di

import com.kiranawala.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Supabase Module - Provides Supabase client and services
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    
    @Singleton
    @Provides
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            
            // Configure JSON serialization to ignore unknown keys
            defaultSerializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
            )
        }
    }
    
    @Singleton
    @Provides
    fun provideSupabaseAuth(client: SupabaseClient): Auth = client.auth
    
    @Singleton
    @Provides
    fun provideSupabaseDatabase(client: SupabaseClient): Postgrest = client.postgrest
    
    @Singleton
    @Provides
    fun provideSupabaseStorage(client: SupabaseClient): Storage = client.storage
}