plugins {
    // build-logic에 정의한 공통 라이브러리 플러그인을 적용합니다.
    id("com.swyp.com.swyp.com.swyp.kitchenlog.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.foodkeeper.feature.kakaologin"
}
dependencies {
    // 이 모듈은 Domain 계층의 규칙을 사용해야 하므로 :core 모듈을 implementation 합니다.
    implementation(project(":core"))


    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose) // viewModel() 사용을 위해 추가

    // Compose UI
    // libs.versions.toml에 정의된 라이브러리들을 사용합니다.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.kakao.sdk.user)

    implementation(libs.hilt.android)
    implementation(libs.coil.compose)

    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}
