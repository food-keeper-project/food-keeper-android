// build-logic/settings.gradle.kts

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // 이 부분이 핵심입니다!
    // 메인 빌드(root project)의 버전 카탈로그를 사용하도록 설정합니다.
// 메인 프로젝트의 버전 카탈로그 설정
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
