package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthRemoteDataSource {
    fun signInWithKakao(kakaoToken: String, fcmToken: String?): Flow<AuthTokenDTO>
    fun refreshToken(accessToken: String,refreshToken:String): Flow<AuthTokenDTO>
}

class DefaultAuthRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) : AuthRemoteDataSource {


    override fun signInWithKakao(
        kakaoToken: String,
        fcmToken: String?
    ): Flow<AuthTokenDTO> {
        return apiService.request(
            ApiRoute.KakaoLogin(
                kakaoAccessToken = kakaoToken,
                mFcmToken = fcmToken
            )
        )
    }

    override fun refreshToken(accessToken:String,refreshToken: String): Flow<AuthTokenDTO> {
        return apiService.request(
            ApiRoute.RefreshToken(
                curAccessToken = accessToken,
                curRefreshToken =refreshToken
            )
        )
    }

}