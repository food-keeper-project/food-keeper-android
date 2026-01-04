// core/src/main/java/com/foodkeeper/core/data/network/SessionManager.kt
package com.foodkeeper.core.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Singleton

@Singleton
object SessionManager {
    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    suspend fun emitLogout() {
        _logoutEvent.emit(Unit)
    }
    suspend fun clearSession() {
        // 1. 저장된 토큰 삭제 (DataStore 등을 사용하는 경우)


        // 2. 메모리 상의 토큰 변수 초기화
        // accessToken = null

        // 3. 로그아웃 이벤트 발생시켜서 화면 이동 유도
        _logoutEvent.emit(Unit)
    }
}
