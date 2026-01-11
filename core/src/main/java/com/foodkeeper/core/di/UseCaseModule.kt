package com.foodkeeper.core.di

import com.foodkeeper.core.domain.usecase.CategoryUseCase
import com.foodkeeper.core.domain.usecase.DefaultCategoryUseCase
import com.foodkeeper.core.domain.usecase.DefaultFoodUseCase
import com.foodkeeper.core.domain.usecase.DefaultSignUpUseCase
import com.foodkeeper.core.domain.usecase.FoodUseCase
import com.foodkeeper.core.domain.usecase.SignUpUseCase
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

    @Binds
    @Singleton
    abstract fun bindCategoryUseCase(
        impl: DefaultCategoryUseCase
    ): CategoryUseCase
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SignUpUseCaseModule {
    @Binds
    @Singleton
    abstract fun bindSignUpUseCase(impl: DefaultSignUpUseCase): SignUpUseCase
}
