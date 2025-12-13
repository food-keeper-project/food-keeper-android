// build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt


import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                // `kanggm` 제외
                defaultConfig.applicationId = "com.foodkeeper"
                defaultConfig.targetSdk = 34
                versionCode = 1
                versionName = "1.0"

                // 나머지 설정은 Library 플러그인과 유사하게 채워넣습니다.
                compileSdk = 34
                defaultConfig.minSdk = 24

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }

                buildFeatures.compose = true
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.14"
                }
            }
        }
    }
}
