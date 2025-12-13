// core, feature 등 라이브러리 모듈용
// build-logic/src/main/kotlin/AndroidLibraryConventionPlugin.kt

import androidx.glance.appwidget.compose
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            // 안드로이드 라이브러리 플러그인 적용
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            // 안드로이드 기본 설정
            extensions.configure<LibraryExtension> {
                compileSdk = 34
                defaultConfig.minSdk = 24

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }

                // Jetpack Compose 설정
                buildFeatures.compose = true
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.14" // 프로젝트에 맞게 조절
                }
            }

            // 공통 의존성 추가 (모든 라이브러리 모듈에 필요한)
            dependencies {
                add("implementation", libs.findLibrary("androidx.core.ktx").get())
            }
        }
    }
}
