package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.External.AutoTokenDTO
import com.foodkeeper.core.data.mapper.External.toUser
import com.foodkeeper.core.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface AuthRepository {
    fun login(kakaoId: String): Flow<User>
}


class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun login(kakaoId: String): Flow<User> {
        return remoteDataSource.login(kakaoId)
            .onEach { dto ->
                // 토큰 저장
                tokenManager.saveTokens(
                    accessToken = dto.accessToken,
                    refreshToken = dto.refreshToken,
                )
            }
            .map { dto ->
                // DTO → Domain Model 변환
                dto.toUser()
            }
    }
}