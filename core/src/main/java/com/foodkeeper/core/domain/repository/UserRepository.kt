package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.mapper.external.ProfileDTO

interface UserRepository {
    suspend fun getMyProfile(): Result<ProfileDTO>
    // 6. 로그아웃
    suspend fun logout(): Result<Unit>

}
