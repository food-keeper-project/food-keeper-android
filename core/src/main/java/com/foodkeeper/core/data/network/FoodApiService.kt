package com.foodkeeper.core.data.network

import android.util.Log
import com.foodkeeper.core.data.datasource.external.AuthRemoteDataSource
import com.foodkeeper.core.data.datasource.local.TokenManager
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.ResultDTO

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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
        try {
            Log.d("FoodApiService", "🚀 요청 시작: ${route.path}")

            val response = executeHttpRequest(route)

            Log.d("FoodApiService", "📥 응답 상태: ${response.status}")

            // ✅ 1️⃣ 201 Created → body 절대 읽지 않음
            if (response.status == HttpStatusCode.Created) {
                handleSuccessResponse(
                    apiResponse = ApiResponse(result = "SUCCESS"),
                    httpResponse = response
                )
                return@flow
            }

            // ✅ 2️⃣ 그 외(200 등)에서만 body 파싱
            val apiResponse = response.body<ApiResponse<T>>()

            // 401 or E3003 체크
            val isExpired = response.status == HttpStatusCode.Unauthorized ||
                    apiResponse.error?.errorCode == "E3003"

            if (isExpired && !route.isRefreshTokenRequest) {
                Log.d("FoodApiService", "토큰 만료 감지 → 재발급 시도")

                val isSuccess = tryRefreshToken()

                if (isSuccess) {
                    val retryResponse = executeHttpRequest(route)

                    // 🔥 재시도도 동일 처리
                    if (retryResponse.status == HttpStatusCode.Created) {
                        handleSuccessResponse(
                            apiResponse = ApiResponse(result = "SUCCESS"),
                            httpResponse = retryResponse
                        )
                        return@flow
                    }

                    val retryApiResponse = retryResponse.body<ApiResponse<T>>()

                    if (retryApiResponse.result == "SUCCESS") {
                        handleSuccessResponse(retryApiResponse, retryResponse)
                    } else {
                        throw ServerException(
                            retryApiResponse.error?.message ?: "알 수 없는 서버 오류",
                            retryApiResponse.error?.errorCode
                        )
                    }
                } else {
                    tokenManagerProvider.get().clearTokens()
                    SessionManager.emitLogout()
                }
            } else {
                if (apiResponse.result == "SUCCESS") {
                    handleSuccessResponse(apiResponse, response)
                } else {
                    throw ServerException(
                        apiResponse.error?.message ?: "알 수 없는 서버 오류",
                        apiResponse.error?.errorCode
                    )
                }
            }
        } catch (e: Exception) {
            if (e is ServerException || e is NetworkException) throw e
            Log.e("FoodApiService", "Unexpected Error", e)
            throw e
        }
    }

    /**
     * ✨ 성공 응답 처리 헬퍼 함수
     * 201 Created 또는 data가 있는 경우를 처리
     */
    @PublishedApi
    internal suspend inline fun <reified T> FlowCollector<T>.handleSuccessResponse(
        apiResponse: ApiResponse<T>,
        httpResponse: HttpResponse
    ) {
        val httpStatus = httpResponse.status
        Log.d("FoodApiService", "API 응답 코드: $httpStatus")

        when {

            // ✅ Case 1: 201 Created → 무조건 ResultDTO 반환
            httpStatus == HttpStatusCode.Created -> {
                Log.d("FoodApiService", "201 Created - ResultDTO 반환")

                @Suppress("UNCHECKED_CAST")
                emit(ResultDTO(result = "SUCCESS") as T)
            }

            // ✅ Case 2: 200 OK + data 있음
            apiResponse.data != null -> {
                Log.d("FoodApiService", "200 OK 응답 - data 포함")

                @Suppress("UNCHECKED_CAST")
                val result = when {
                    T::class == ResultDTO::class && apiResponse.data is String -> {
                        ResultDTO(result = apiResponse.data as String) as T
                    }
                    else -> apiResponse.data as T
                }

                emit(result)
            }

            // ✅ Case 3: 200 OK + data 없음
            else -> {
                Log.d("FoodApiService", "200 OK 응답 - data 없음")

                @Suppress("UNCHECKED_CAST")
                emit(ResultDTO(result = "SUCCESS") as T)
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
            val accessToken = tokenManager.accessToken.first()

            method = route.method

            if (!route.multiPartRequest) {
                contentType(ContentType.Application.Json)
            }

            // 헤더 설정
            if (route.requiresAuth) {
                header("Authorization", "Bearer $accessToken")
                Log.d("FoodApiService", "[${route.path} Header 주입 완료: Bearer $accessToken")
            }else{
                // 만약 ApiRoute 내부에서 이미 처리가 되어있다면 그대로 사용

                route.headers.forEach { (key, value) -> header(key, value) }
            }

            // 3. 바디 설정 (여기가 중요 ⭐)
            route.body?.let {
                if (route.multiPartRequest) {
                    // ✅ 멀티파트일 때는 ContentType을 명시적으로 세팅하지 않음 (Ktor가 자동 생성)
                    setBody(it)
                } else {
                    // 일반 JSON 요청일 때만 ContentType 설정
                    contentType(ContentType.Application.Json)
                    setBody(it)
                }
            }
            route.queryParameters.forEach { (key, value) -> parameter(key, value) }

            route.timeoutMillis?.let {
                timeout { requestTimeoutMillis = it }
            }
        }
    }
}
class ServerException(message: String, val errorCode: String?) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
class NetworkException(message: String, cause: Throwable) : Exception(message, cause)