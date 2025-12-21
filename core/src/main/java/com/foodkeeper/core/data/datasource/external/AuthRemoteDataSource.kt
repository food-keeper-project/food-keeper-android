package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.External.AutoTokenDTO
import com.foodkeeper.core.data.network.ApiResult
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface AuthRemoteDataSource {
    fun login(kakaoId: String): Flow<AutoTokenDTO>
}

class DefaultAuthRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) : AuthRemoteDataSource {

    override fun login(kakaoId: String): Flow<AutoTokenDTO> {
        return apiService.request(
            ApiRoute.Login(kakaoID = kakaoId)
        )
    }

}