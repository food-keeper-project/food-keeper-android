package com.swyp.foodkeeper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FoodKeeperApplication: Application() , ImageLoaderFactory{
    @Inject
    lateinit var imageLoader: ImageLoader
    override fun newImageLoader(): ImageLoader = imageLoader
    override fun onCreate() {
        super.onCreate()
        // FirebaseCrashlytics 실행
        FirebaseCrashlytics.getInstance()
    }


}