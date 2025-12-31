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
}
