// app/src/main/kotlin/com/foodkeeper/MyApplication.kt

package com.example.foodkeeper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: ImageLoader
    override fun newImageLoader(): ImageLoader = imageLoader
    override fun onCreate() {
        super.onCreate()

        // 위에서 사용한 네이티브 앱 키를 사용하여 Kakao SDK를 초기화합니다.
        KakaoSdk.init(this, "019455b78c75b46224c1828903b55643")
    }
}
