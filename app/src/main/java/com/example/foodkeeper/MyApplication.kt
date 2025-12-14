// app/src/main/kotlin/com/foodkeeper/MyApplication.kt

package com.example.foodkeeper

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 위에서 사용한 네이티브 앱 키를 사용하여 Kakao SDK를 초기화합니다.
        KakaoSdk.init(this, "019455b78c75b46224c1828903b55643")
    }
}
