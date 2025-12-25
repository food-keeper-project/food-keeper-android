package com.foodkeeper.core.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refreshToken")
        private val USER_ID_KEY = stringPreferencesKey("userID")
        val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("hasSeenOnboarding")
    }
    // 함수가 아니라 'Flow 프로퍼티'로 정의하는 것이 일반적입니다.
    val accessToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    // 온보딩 상태 읽기
    val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { it[HAS_SEEN_ONBOARDING_KEY] == true }

    // 온보딩 완료 처리
    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] = completed
        }
    }
    /**
     * 토큰 저장
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String? = null,
        expiresAt: String? = null
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            userId?.let { prefs[USER_ID_KEY] = it }
        }
    }

    /**
     * Access Token 가져오기
     */
    suspend fun getAccessToken(): String? {
        return dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN_KEY]
        }.first()
    }

    /**
     * Refresh Token 가져오기
     */
    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }.first()
    }

    /**
     * User ID 가져오기
     */
    suspend fun getUserId(): String? {
        return dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }.first()
    }


    /**
     * 토큰 존재 여부 확인 (로그인 상태 체크)
     */
    suspend fun hasValidTokens(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }


    /**
     * 모든 토큰 삭제 (로그아웃)
     */
    suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
        }
    }

    /**
     * Access Token만 업데이트 (토큰 갱신 시)
     */
    suspend fun updateAccessToken(accessToken: String, expiresAt: String? = null) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
        }
    }
}