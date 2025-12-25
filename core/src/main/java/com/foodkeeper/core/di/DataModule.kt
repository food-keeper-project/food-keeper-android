package com.foodkeeper.core.di

import com.foodkeeper.core.domain.repository.UserRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Singleton
    abstract fun bindUserRepository(
    ): UserRepository

}