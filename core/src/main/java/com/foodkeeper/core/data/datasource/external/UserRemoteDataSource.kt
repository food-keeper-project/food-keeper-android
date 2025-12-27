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
}
