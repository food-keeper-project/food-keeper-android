package com.example.foodkeeper.di

import com.foodkeeper.core.domain.usecase.DefaultFoodUseCase
import com.foodkeeper.core.domain.usecase.FoodUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodUseCaseModule {

    @Binds
    @Singleton
    abstract fun bindFoodUseCase(
        impl: DefaultFoodUseCase
    ): FoodUseCase
}