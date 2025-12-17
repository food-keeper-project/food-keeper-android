package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.network.FoodApiService
import com.foodkeeper.core.data.network.ApiRoute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface UserRemoteDataSource {
    suspend fun login(token: String): LoginResponse
//    suspend fun refreshToken(): LoginResponse
//    suspend fun logout(userId: String)
}

class UserRemoteDataSourceImpl @Inject constructor(
    private val apiService: FoodApiService
) : UserRemoteDataSource {

    override suspend fun login(token: String): LoginResponse {
        return apiService.request(
            ApiRoute.Login(token)
        )
    }

//    override suspend fun refreshToken(): LoginResponse {
//        return apiService.request(
//            ApiRoute.RefreshToken()
//        )
//    }
//
//    override suspend fun logout(userId: String) {
//        apiService.request<Unit>(
//            ApiRoute.Logout(userId)
//        )
//    }
}