// feature/splash/build.gradle.kts

plugins {
    // 공통 라이브러리 설정을 적용합니다. (Android 설정, Compose 기초 등 포함되어 있을 것임)
    id("foodkeeper.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.foodkeeper.feature.splash"
}

dependencies {
    // 핵심 로직과 모델을 사용하기 위해 core 모듈 의존
    implementation(project(":core"))

    // ViewModel 및 Compose 연동
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose) // collectAsStateWithLifecycle 사용을 위해 권장

    // Compose UI (BOM을 사용하여 버전 관리)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // DI (Hilt)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}
