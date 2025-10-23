package com.kiranawala.di

import android.content.Context
import androidx.room.Room
import com.kiranawala.data.local.db.AppDatabase
import com.kiranawala.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kiranawala.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    
    @Provides
    fun provideStoreDao(db: AppDatabase): StoreDao = db.storeDao()
    
    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()
    
    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()
    
    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()
    
    @Provides
    fun provideStoreReviewDao(db: AppDatabase): StoreReviewDao = db.storeReviewDao()
}
