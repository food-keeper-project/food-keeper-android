// core/build.gradle.kts

// 안드로이드 라이브러리 플러그인을 적용합니다.
plugins {
    id("foodkeeper.android.library")
}
android {
    namespace = "com.foodkeeper.core"
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    // core 모듈에서 필요한 공통 라이브러리를 여기에 추가합니다.
    // 예: implementation("androidx.core:core-ktx:1.12.0")
    // core 모듈에만 필요한 특별한 의존성만 남깁니다.
    implementation(libs.kotlinx.coroutines.core)
}
