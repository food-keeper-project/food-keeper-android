package com.foodkeeper.core.data.network

import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Provider // Hilt에서 Lazy 주입을 위해 사용
// ... 상단 임포트에 추가
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class FoodApiService @Inject constructor(
    private val client: HttpClient,
    // 순환 참조 방지를 위해 Provider(Lazy) 사용
    @PublishedApi internal val tokenManagerProvider: Provider<TokenManager>,
    @PublishedApi internal val authRemoteDataSourceProvider: Provider<AuthRemoteDataSource>
) {
    // ✅ 동시 재발급 방지를 위한 Mutex 선언
    companion object {
        private val refreshTokenMutex = Mutex()
    }
    inline fun <reified T> request(
        route: ApiRoute
    ): Flow<T> = flow {
        // 1. 첫 번째 요청 실행
        var response = executeHttpRequest(route)

        // 💡 응답 바디를 미리 역직렬화하여 에러 코드를 확인합니다.
        // Ktor의 body()는 한 번 읽으면 소비되므로, 결과를 변수에 저장합니다.
        val initialApiResponse = response.body<ApiResponse<T>>()

        // 2. 401 Unauthorized 또는 에러 코드가 E3003일 때 재발급 로직 진입
        val isExpired = response.status == HttpStatusCode.Unauthorized ||
                initialApiResponse.error?.errorCode == "E3003"

        if (isExpired && !route.isRefreshTokenRequest) {
            Log.d("FoodApiService", "토큰 만료 감지 (E3003) -> 재발급 시도")

            val isSuccess = tryRefreshToken()

            if (isSuccess) {
                Log.d("FoodApiService", "재발급 성공 -> 원래 요청 재시도")
                response = executeHttpRequest(route)

                // 재시도한 결과를 다시 읽음
                val retryApiResponse = response.body<ApiResponse<T>>()
                if (retryApiResponse.result == "SUCCESS" && retryApiResponse.data != null) {
                    emit(retryApiResponse.data)
                } else {
                    throw ServerException(
                        message = retryApiResponse.error?.message ?: "알 수 없는 서버 오류",
                        errorCode = retryApiResponse.error?.errorCode
                    )
                }
            } else {
                Log.e("FoodApiService", "재발급 실패 (리프레시 토큰 만료) -> 로그인 필요")
                // ✅ 토큰 삭제 로직 추가 권장
                tokenManagerProvider.get().clearTokens()
                //세션 매니저로 로그아웃 보내서 메인화면에서 이벤트 감지하기
                SessionManager.emitLogout()
                //throw UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요.")
            }
        } else {
            // 3. 만료 상황이 아니면 첫 번째 결과를 그대로 처리
            if (initialApiResponse.result == "SUCCESS" && initialApiResponse.data != null) {
                emit(initialApiResponse.data)
            } else {
                throw ServerException(
                    message = initialApiResponse.error?.message ?: "알 수 없는 서버 오류",
                    errorCode = initialApiResponse.error?.errorCode
                )
            }
        }

    }

    /**
     * 재발급 로직을 수행하는 내부 함수
     */
    @PublishedApi
    internal suspend fun tryRefreshToken(): Boolean {
        // 1️⃣ 현재 내가 알고 있는 "만료된" 토큰을 미리 기억해둡니다.
        val oldTokenAtEntry = tokenManagerProvider.get().accessToken.first()

        return refreshTokenMutex.withLock {
            try {
                val tokenManager = tokenManagerProvider.get()
                // 2️⃣ 잠금이 풀려 진입했을 때, 다시 한번 현재 토큰을 확인합니다.
                val currentToken = tokenManager.accessToken.first()
                // 💡 만약 누군가 이미 토큰을 업데이트했다면 (내가 아까 본 토큰과 다르다면)
                // 굳이 서버에 또 요청할 필요 없이 성공으로 간주하고 나갑니다.
                if (currentToken != oldTokenAtEntry && !currentToken.isNullOrEmpty()) {
                    Log.d("FoodApiService", "이미 다른 요청에 의해 토큰이 갱신됨. 재발급 건너뜀.")
                    return true
                }


                // --- 이후 기존 로직 ---
                val oldRefreshToken = tokenManager.refreshToken.first()
                Log.d("FoodApiService", "로컬 리프레시 토큰: $oldRefreshToken")
                if (oldRefreshToken.isNullOrEmpty()) {
                    Log.e("FoodApiService", "리프레시 토큰이 없어 재발급 불가")
                    return false
                }

                Log.d("FoodApiService", "재발급 요청 시작...")
                val response = executeHttpRequest(ApiRoute.RefreshToken(currentToken ?: "", oldRefreshToken))
                val result = response.body<ApiResponse<AuthTokenDTO>>()

                if (result.result == "SUCCESS" && result.data != null) {
                    tokenManager.saveTokens(
                        accessToken = result.data.accessToken ?: currentToken ?: "",
                        refreshToken = result.data.refreshToken ?: oldRefreshToken
                    )
                    Log.d("FoodApiService", "새 토큰 저장 완료")
                    true
                } else {
                    Log.e("FoodApiService", "서버에서 재발급 거절: ${result.error?.errorCode}")
                    false
                }
            } catch (e: Exception) {
                // ✅ 여기서 CancellationException은 로그를 찍지 않거나 정상 처리로 넘겨야 깔끔합니다.
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("FoodApiService", "재발급 과정 중 예외 발생: ${e.message}")
                false
            }
        }
    }

    @PublishedApi
    internal suspend fun executeHttpRequest(
        route: ApiRoute
    ): HttpResponse {
        return client.request(route.baseURL + route.path) {
            val tokenManager = tokenManagerProvider.get()
            val accessToken=tokenManager.accessToken.first()
            method = route.method
            if (!route.multiPartRequest) {
                contentType(ContentType.Application.Json)
            }


            // 헤더 설정
            // [중요] 일반 요청 시에는 DataStore에서 최신 액세스 토큰을 가져와야 함

            // ✅ 3. 일반 요청(not Refresh)이면서 토큰이 존재하는 경우 Authorization 헤더 강제 주입
            if (route.requiresAuth) {
                header("Authorization", "Bearer $accessToken")
                Log.d("FoodApiService", "[${route.path} Header 주입 완료: Bearer $accessToken")
            }else{
                // 만약 ApiRoute 내부에서 이미 처리가 되어있다면 그대로 사용
                route.headers.forEach { (key, value) -> header(key, value) }
            }

            // 쿼리 및 바디 설정
            route.body?.let { setBody(it) }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }

            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        }
    }
}

class ServerException(message: String, val errorCode: String?) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
