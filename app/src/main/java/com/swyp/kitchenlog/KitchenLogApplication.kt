package com.swyp.kitchenlog

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KitchenLogApplication : Application() , ImageLoaderFactory{
    @Inject
    lateinit var imageLoader: ImageLoader
    override fun newImageLoader(): ImageLoader = imageLoader
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, "019455b78c75b46224c1828903b55643")
        // FirebaseCrashlytics 실행
        FirebaseCrashlytics.getInstance()
    }


}
