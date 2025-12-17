package com.foodkeeper.core.di

import com.foodkeeper.core.data.repository.UserRepositoryImpl
import com.foodkeeper.core.domain.repository.UserRepository
import com.google.android.datatransport.runtime.dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repository: UserRepositoryImpl
    ): UserRepository

}