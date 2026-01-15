package com.foodkeeper.core.data.repository

import android.util.Log
import com.foodkeeper.core.data.datasource.external.UserRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton // 앱이 켜져 있는 동안 이 객체는 하나만 유지됩니다.
class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val tokenManager: TokenManager
) : UserRepository {


    override suspend fun getMyProfile(): Result<ProfileDTO> {
        // 1. 이미 이번 세션에서 한 번 불러온 적이 있다면? (캐시 존재)
        // 서버에 가지 않고 즉시 메모리에 있는 값을 반환합니다.
        // 2. 캐시가 없다면(앱을 새로 켰거나 처음 호출한 경우)? 서버에서 가져옵니다.
        return try {
            val remoteProfile = userRemoteDataSource.getMyProfile().first()

            // 3. 서버에서 받은 데이터를 메모리에 저장합니다.
            // 이제 앱을 끌 때까지 1번 로직에 의해 서버 통신이 발생하지 않습니다.

            Result.success(remoteProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅✅✅ 핵심 수정: 로그인 타입에 따라 분기하는 로그아웃 로직 ✅✅✅
    override suspend fun logout(): Result<Unit> {
        return try {
            // 1. 저장된 마지막 로그인 방식을 가져옵니다.
            val lastLoginType = tokenManager.loginType.first()

            // 3. (공통) 우리 서버에 로그아웃 API를 호출합니다. (선택적이지만 권장)
            // 서버에 FCM 토큰을 삭제해달라고 요청하는 등의 역할을 합니다.
            // 실패하더라도 로컬 토큰은 반드시 지워야 하므로 collect는 비워둡니다.
            userRemoteDataSource.logOut().catch { e ->
                Log.w("AuthRepoImpl", "Server logout API failed: ${e.message}")
            }.collect()

            // 4. (가장 중요) 기기에 저장된 모든 로컬 토큰을 삭제합니다.
            tokenManager.clearTokens()


            // 5. 모든 절차가 끝나면 최종 성공을 반환합니다.
            Result.success(Unit)

        } catch (e: Exception) {
            // 위 과정 중 예기치 못한 에러(e.g., DataStore 접근 실패) 발생 시
            Log.e("AuthRepoImpl", "Logout failed with an unexpected exception.", e)
            // 방어적으로 한 번 더 토큰 삭제를 시도합니다.
            try {
                tokenManager.clearTokens()

            } catch (clearError: Exception) {
                Log.e("AuthRepoImpl", "Failed to clear tokens in catch block.", clearError)
            }
            Result.failure(e)
        }
    }

}
