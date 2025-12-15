// build-logic/build.gradle.kts

import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue

plugins {
    // Kotlin으로 Gradle 플러그인을 작성하기 위한 플러그인
    `kotlin-dsl`
}
repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}
// Gradle이 이 모듈의 플러그인을 찾을 수 있도록 그룹 ID 설정
group = "com.foodkeeper.buildlogic"
// 버전 카탈로그(libs.versions.toml)에 접근하기 위한 변수 선언
val libs = extensions.getByType<org.gradle.accessors.dm.LibrariesForLibs>()

// build-logic 모듈이 필요로 하는 의존성 추가
dependencies {

    // Gradle, Android Gradle Plugin, Kotlin Gradle Plugin의 API를 사용하기 위해 추가
    implementation(gradleApi())
    // libs.versions.toml에서 버전을 가져오도록 수정
    // 안드로이드 애플리케이션 플러그인 API
//    implementation(libs.plugins.android.application)
//    // 코틀린 안드로이드 플러그인 API
//    implementation(libs.plugins.kotlin.android)

    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)

}

// 우리가 만들 커스텀 플러그인들을 Gradle에 정식으로 등록하는 과정
gradlePlugin {
    plugins {
        // "플러그인 이름" to (id = "ID", implementationClass = "클래스 경로")
        register("androidApplication") {
            id = "foodkeeper.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "foodkeeper.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
