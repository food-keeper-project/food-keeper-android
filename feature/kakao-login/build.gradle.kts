
// feature/kakaologin/build.gradle.kts

plugins {
    // build-logic에 정의한 공통 라이브러리 플러그인을 적용합니다.
    id("foodkeeper.android.library")
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

    // Compose UIㅈ
    // libs.versions.toml에 정의된 라이브러리들을 사용합니다.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
}
