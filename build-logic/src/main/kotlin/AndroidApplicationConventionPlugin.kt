// build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt

package com.foodkeeper.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion // 1. JavaVersion을 사용하기 위해 import 추가
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                compileSdk = 34

                defaultConfig {
                    applicationId = "com.foodkeeper"
                    targetSdk = 34
                    minSdk = 24

                    // 2. versionCode와 versionName을 defaultConfig 블록 안으로 이동
                    versionCode = 1
                    versionName = "1.0"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }


                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.14"
                }
            }
        }
    }
}
