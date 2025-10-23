package com.kiranawala.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kiranawala.data.local.dao.*
import com.kiranawala.data.local.entities.*
import com.kiranawala.data.local.typeconverter.DateTypeConverter

@Database(
    entities = [
        CustomerEntity::class,
        StoreEntity::class,
        ProductEntity::class,
        CartEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        StoreReviewEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun storeReviewDao(): StoreReviewDao
}
