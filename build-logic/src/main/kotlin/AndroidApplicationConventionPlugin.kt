// build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1. 필요한 플러그인 적용 (이 부분은 동일합니다)
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")
            // 컴포즈 컴파일러 플러그인을 직접 적용해야 합니다.
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // 2. 안드로이드 애플리케이션 관련 설정
            extensions.configure<ApplicationExtension> {
                compileSdk = 36
                defaultConfig {
                    applicationId = "com.foodkeeper"
                    targetSdk = 36
                    minSdk = 24
                    versionCode = 1
                    versionName = "1.0"
                }

                // Java 버전 설정 (이 부분은 맞습니다)
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                // Compose 사용 설정 (이 부분도 맞습니다)
                buildFeatures {
                    compose = true
                }
            }

            // 3. 코틀린 관련 설정 (JVM 버전을 여기서 설정합니다)
            extensions.configure<KotlinAndroidProjectExtension> {
                jvmToolchain(17)
            }
        }
    }
}
