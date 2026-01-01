package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.mapper.external.ProfileDTO

interface UserRepository {
    suspend fun getMyProfile(): Result<ProfileDTO>

}
