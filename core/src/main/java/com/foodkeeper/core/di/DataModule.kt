package com.foodkeeper.core.di
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.external.DefaultAuthRemoteDataSource
import com.foodkeeper.core.data.repository.AuthRepositoryImpl
import com.foodkeeper.core.data.repository.FoodRepositoryImpl
import com.foodkeeper.core.data.repository.UserRepository
import com.foodkeeper.core.data.repository.UserRepositoryImpl
import com.foodkeeper.core.domain.repository.AuthRepository
import com.foodkeeper.core.domain.repository.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        // DefaultAuthRemoteDataSource를 AuthRemoteDataSource 인터페이스에 연결
        authRemoteDataSourceImpl: DefaultAuthRemoteDataSource
    ): AuthRemoteDataSource
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        foodRepositoryImpl: FoodRepositoryImpl
    ): FoodRepository

}