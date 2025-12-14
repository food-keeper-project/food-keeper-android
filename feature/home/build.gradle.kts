plugins {
    id("foodkeeper.android.library")
}
android {
    namespace = "com.foodkeeper.feature.home"
}
dependencies {
    implementation(project(":core"))

    // Compose, ViewModel 등 home 모듈에만 필요한 의존성만 남깁니다.
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    // ...
}
