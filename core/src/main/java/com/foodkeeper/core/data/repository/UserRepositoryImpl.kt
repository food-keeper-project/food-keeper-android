package com.foodkeeper.core.data.repository

import com.foodkeeper.core.data.datasource.external.UserRemoteDataSource
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱이 켜져 있는 동안 이 객체는 하나만 유지됩니다.
class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    // ✅ 앱 프로세스가 살아있는 동안만 유지되는 메모리 캐시 변수
    private var cachedProfile: ProfileDTO? = null

    override suspend fun getMyProfile(): Result<ProfileDTO> {
        // 1. 이미 이번 세션에서 한 번 불러온 적이 있다면? (캐시 존재)
        // 서버에 가지 않고 즉시 메모리에 있는 값을 반환합니다.
        cachedProfile?.let {
            return Result.success(it)
        }

        // 2. 캐시가 없다면(앱을 새로 켰거나 처음 호출한 경우)? 서버에서 가져옵니다.
        return try {
            val remoteProfile = userRemoteDataSource.getMyProfile().first()

            // 3. 서버에서 받은 데이터를 메모리에 저장합니다.
            // 이제 앱을 끌 때까지 1번 로직에 의해 서버 통신이 발생하지 않습니다.
            cachedProfile = remoteProfile

            Result.success(remoteProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ 로그아웃 구현
    override suspend fun logout(): Result<String> {
        return try {
            // 1. 서버 로그아웃 요청
            val response = userRemoteDataSource.logOut().first()

            // 2. 로그아웃 성공 시(또는 실패와 상관없이) 메모리 캐시 비우기
            clearCache()

            Result.success(response)
        } catch (e: Exception) {
            // 서버 통신에 실패하더라도 로컬 캐시는 비워주는 것이 안전합니다.
            clearCache()
            Result.failure(e)
        }
    }

    // 로그아웃 시 캐시를 비워줘야 다음 로그인 때 이전 사용자 정보가 안 보입니다.
    fun clearCache() {
        cachedProfile = null
    }
}
