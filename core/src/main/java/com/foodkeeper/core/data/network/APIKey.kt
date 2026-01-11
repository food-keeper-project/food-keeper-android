package com.foodkeeper.core.data.network

import com.foodkeeper.core.BuildConfig


object ApiKey {
    // ✅ BuildConfig.DEBUG 값에 따라 BASE_URL이 자동으로 결정됨
    val BASE_URL = if (BuildConfig.DEBUG) {
        "https://dev.kitchenlog.shop/" // 디버그 모드일 때 (개발용)
    } else {
        "https://prod.kitchenlog.shop/" // 릴리즈 모드일 때 (실서버용)
    }
    const val KAKAO_KEY = ""
}
