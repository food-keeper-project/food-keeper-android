package com.foodkeeper.core.di
<<<<<<< HEAD

import com.foodkeeper.core.domain.repository.UserRepository
=======
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.external.DefaultAuthRemoteDataSource
import com.foodkeeper.core.data.repository.AuthRepositoryImpl
import com.foodkeeper.core.domain.repository.AuthRepository
import dagger.Binds
>>>>>>> 3da4138d8d0621909c430a342f904ff9df8f90b9
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
<<<<<<< HEAD

    @Singleton
    abstract fun bindUserRepository(
    ): UserRepository
=======
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
>>>>>>> 3da4138d8d0621909c430a342f904ff9df8f90b9

}