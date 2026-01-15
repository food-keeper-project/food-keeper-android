
plugins {
    // 1. 공통 라이브러리 설정을 적용합니다.
    id("com.swyp.foodkeeper.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization) // @Serializable 사용을 위해 필요
}

android {
    // 이 모듈의 고유 패키지 명을 지정합니다.
    namespace = "com.foodkeeper.feature.airecipe"
}

dependencies {
    implementation(project(":core"))
    // ✅ Coil (프로필 이미지 로딩 필수)
    implementation(libs.coil.compose) // libs.versions.toml에 정의되어 있어야 함

    // ✅ Material 3 (UI 구성 필수)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // ✅ Icons (Person, ArrowRight 아이콘 사용 필수)
    implementation(libs.androidx.compose.material.icons.extended)

    // ✅ Hilt & Navigation
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ✅ Lifecycle (collectAsStateWithLifecycle 사용 필수)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
