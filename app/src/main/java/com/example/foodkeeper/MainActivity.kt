package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import AiRecipeDetailScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foodkeeper.feature.kakaologin.LoginScreen      // 모듈 이름 'kakao-login'에 맞게 수정
import com.example.foodkeeper.ui.theme.FoodKeeperTheme     // 패키지 이름에 맞게 수정
import com.foodkeeper.feature.profile.ProfileRoute
import com.foodkeeper.feature.splash.OnboardingScreen
import com.foodkeeper.feature.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodKeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 메인 진입점에서 내비게이션 함수 호출
                    FoodKeeperNavHost()
                }
            }
        }
    }
}
/**
* 앱 전체의 화면 흐름을 관리하는 함수
*/
@Composable
fun FoodKeeperNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash" // 앱 시작 시 스플래시를 먼저 띄움
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        // 뒤로가기 눌렀을 때 온보딩으로 다시 오지 않게 제거
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        // 1. 스플래시 화면
        composable("splash") {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("ai_recipe_detail") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. 로그인 화면
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 3. 메인 화면 (임시)
        composable("main") {
            Surface(modifier = Modifier.fillMaxSize()) {
                // 나중에 만들 메인 화면 연결
                // MainScreen()
            }
        }
        // 4. 마이페이지
        composable("profile") {
            ProfileRoute(
                onNavigateToHistory = {                    // navController.navigate("history") // 이동할 경로가 정의되면 연결
                },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        // 5. AI레시피 상세화면
        composable("ai_recipe_detail"){
            AiRecipeDetailScreen()
        }
    }
}

