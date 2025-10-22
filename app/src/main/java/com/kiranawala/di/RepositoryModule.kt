package com.kiranawala.di

import com.kiranawala.data.repositories.AddressRepositoryImpl
import com.kiranawala.data.repositories.AuthRepositoryImpl
import com.kiranawala.data.repositories.CartRepositoryImpl
import com.kiranawala.data.repositories.OrderRepositoryImpl
import com.kiranawala.data.repositories.ProductRepositoryImpl
import com.kiranawala.data.repositories.ProfileRepositoryImpl
import com.kiranawala.data.repositories.StoreRepositoryImpl
import com.kiranawala.domain.repositories.AddressRepository
import com.kiranawala.domain.repositories.AuthRepository
import com.kiranawala.domain.repositories.CartRepository
import com.kiranawala.domain.repositories.OrderRepository
import com.kiranawala.domain.repositories.ProductRepository
import com.kiranawala.domain.repositories.ProfileRepository
import com.kiranawala.domain.repositories.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Singleton
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    
    @Singleton
    @Binds
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository
    
    @Singleton
    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
    
    @Singleton
    @Binds
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository
    
    @Singleton
    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
    
    @Singleton
    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
    
    @Singleton
    @Binds
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository
    
    // TODO: Add in Phase 5
    // @Singleton
    // @Binds
    // abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository
}
