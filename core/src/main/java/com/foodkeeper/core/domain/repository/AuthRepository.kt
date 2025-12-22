package com.foodkeeper.core.domain.repository

import com.foodkeeper.core.domain.model.LoginResult
import com.foodkeeper.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 인증(Authentication) 관련 데이터 처리를 위한 Repository 인터페이스.
 * Domain 레이어에 속하며, 데이터 소스(카카오, 구글, 자체서버 등)에 대한 구체적인 내용은 숨깁니다.
 */
interface AuthRepository{

    /**
     * 소셜 로그인을 시도하고 그 결과를 Flow 형태로 반환합니다.
     * Flow를 사용하면 로그인 과정에서 발생하는 여러 상태(로딩, 성공, 실패, 취소)를
     * 비동기 스트림으로 우아하게 처리할 수 있습니다.
     *
     * @return LoginResult의 흐름(Flow)을 반환합니다.
     */
    fun login(): Flow<LoginResult>
    fun login(kakaoId: String): Flow<User>
    // 현재 로그인된 토큰이 있는지 확인 (Flow로 실시간 관찰하거나 단발성으로 확인)
    fun hasToken(): Flow<Boolean>
    // 1. 관찰(Read): 데이터가 바뀌면 자동으로 알려줌
    fun hasSeenOnboarding():Flow<Boolean>
    // 2. 명령(Write): 딱 한 번 저장 작업을 수행함
    suspend fun saveOnboardingStatus(completed: Boolean)
}
