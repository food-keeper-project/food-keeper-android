package com.foodkeeper.core.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.engine.cio.CIO

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                }
            }
            // JSON 직렬화 설정 (API 응답을 객체로 변환하기 위해 필요)
            install(ContentNegotiation) {
                // 1. SSL 인증서 검증 우회 (Trust anchor... 에러 해결용)

                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // 서버에서 예상치 못한 키를 보내도 에러 안 나게 함
                    encodeDefaults = true    // 기본값도 JSON에 포함
                })
            }
            // 로깅 설정 (수정됨)
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
                level = LogLevel.ALL // io.ktor.client.plugins.logging.LogLevel 사용
            }

            // 여기에 로깅이나 타임아웃 설정을 추가할 수 있습니다.

        }
    }
}
