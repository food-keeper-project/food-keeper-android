package com.foodkeeper.core.data.datasource.external

import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.data.network.ApiRoute
import com.foodkeeper.core.data.network.FoodApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 하나로 관리할 때는 Singleton 어노테이션을 붙여주는 것이 좋습니다.
class UserRemoteDataSource @Inject constructor(
    private val apiService: FoodApiService
) {
    // 내 프로필 정보 조회
    fun getMyProfile(): Flow<ProfileDTO> {
        return apiService.request(ApiRoute.MyProfile)
    }
    // ✅ 로그아웃 추가
    fun logOut(): Flow<String> {
        // FoodApiService.request<T>가 내부적으로 ApiResponse<T>를 파싱해서
        // data(T)만 내보내도록 구현되어 있다면 아래와 같이 작성합니다.
        return apiService.request(ApiRoute.Logout)
    }
}
