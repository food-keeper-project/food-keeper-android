pluginManagement {
    // 이 부분이 있어야 Gradle이 build-logic을 인식합니다.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 카카오 SDK는 이 저장소에만 존재합니다.
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }

}

rootProject.name = "FoodKeeper"
include(":app")
include(":core")
include(":feature")
include(":feature:home")
include(":feature:kakao-login")
include(":feature:splash")
include(":feature:profile")
include(":feature:ai-recipe")
 