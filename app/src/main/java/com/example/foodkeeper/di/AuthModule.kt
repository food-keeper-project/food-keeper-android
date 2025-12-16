package com.foodkeeper.di

import android.content.Context
import com.foodkeeper.core.domain.repository.AuthRepository
import com.example.foodkeeper.data.repository.KakaoAuthRepositoryImpl
import com.foodkeeper.feature.kakaologin.LoginViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository {
        // AuthRepository 인터페이스의 구현체로 KakaoAuthRepositoryImpl을 제공한다고 Hilt에 알립니다.
        return KakaoAuthRepositoryImpl(context)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideLoginViewModel(
        authRepository: AuthRepository // Hilt가 위에서 만든 KakaoAuthRepositoryImpl을 자동으로 주입해줍니다.
    ): LoginViewModel {
        // AuthRepository를 주입받아 LoginViewModel을 생성합니다.
        return LoginViewModel(authRepository)
    }
}
