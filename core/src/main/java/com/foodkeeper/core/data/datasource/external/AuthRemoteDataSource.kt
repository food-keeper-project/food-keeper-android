package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.External.AutoTokenDTO
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import javax.inject.Inject

interface AuthRemoteDataSource {
//    suspend fun login(kakaoId: String): LoginResponseDTO
    suspend fun refreshToken(refreshToken: String): AutoTokenDTO
}

class AuthRemoteDataSourceImpl @Inject constructor(
    private val apiService: FoodApiService
) : AuthRemoteDataSource {

//    override suspend fun login(kakaoId: String): LoginResponseDTO {
//        return apiService.request(
//            ApiRoute.Login(kakaoID = kakaoId)
//        )
//    }

    override suspend fun refreshToken(refreshToken: String): AutoTokenDTO {
        return apiService.request(
            ApiRoute.RefreshToken(token = refreshToken)
        )
    }
}