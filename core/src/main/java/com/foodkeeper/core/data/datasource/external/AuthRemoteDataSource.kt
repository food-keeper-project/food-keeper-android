package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.respone.AccountResponseDTO
import com.foodkeeper.core.data.mapper.request.AccountRequestDTO
import com.foodkeeper.core.data.mapper.request.SignUpRequestDTO
import com.foodkeeper.core.data.network.ApiResult
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthRemoteDataSource {
    fun signInWithKakao(kakaoToken: String, fcmToken: String?): Flow<AuthTokenDTO>
    fun refreshToken(accessToken: String,refreshToken:String): Flow<AuthTokenDTO>
    fun withdrawAccount(): Flow<String>
    // ✅ 로그아웃 추가
    fun logOut(): Flow<String>
    fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO>
    fun signUp(userId: String, userPw: String, email: String, gender:String, nickname:String): Flow<String>
    fun verifyEmail(email: String): Flow<String>
    fun verifyEmailCode(email: String, code: String): Flow<String>
    fun signIn(userId: String, userPw: String, fcmToken: String?): Flow<AuthTokenDTO>
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

    override fun withdrawAccount(): Flow<String> {
        return apiService.request(ApiRoute.WithdrawAccount)
    }

    override fun logOut(): Flow<String> {
            // FoodApiService.request<T>가 내부적으로 ApiResponse<T>를 파싱해서
            // data(T)만 내보내도록 구현되어 있다면 아래와 같이 작성합니다.
            return apiService.request(ApiRoute.Logout)
    }

    override fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO> {
        return apiService.request(ApiRoute.PostCheckAccount(AccountRequestDTO(account = userId)))
    }

    override fun signUp(
        userId: String,
        userPw: String,
        email: String,
        gender:String,
        nickname:String
    ): Flow<String> {
        return apiService.request(ApiRoute.PostSignUp(SignUpRequestDTO(userId,userPw,email,nickname,gender)))
    }

    override fun verifyEmail(email: String): Flow<String> {
        return apiService.request(ApiRoute.PostVerifyEmail(email))
    }

    override fun verifyEmailCode(
        email: String,
        code: String
    ): Flow<String> {
        return apiService.request(ApiRoute.PostVerifyEmailCode(email, code))
    }

    override fun signIn(
        userId: String,
        userPw: String,
        fcmToken: String?
    ): Flow<AuthTokenDTO> {
        return apiService.request(ApiRoute.LocalLogin(userId,userPw,fcmToken))
    }

}