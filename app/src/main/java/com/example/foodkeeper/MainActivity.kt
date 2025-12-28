package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foodkeeper.feature.kakaologin.LoginScreen      // 모듈 이름 'kakao-login'에 맞게 수정
import com.example.foodkeeper.ui.theme.FoodKeeperTheme     // 패키지 이름에 맞게 수정
import com.foodkeeper.feature.airecipe.AiRecipeDetailScreen
import com.foodkeeper.feature.airecipe.AiRecipeHistoryScreen
import com.foodkeeper.feature.profile.ProfileRoute
import com.foodkeeper.feature.splash.OnboardingScreen
import com.foodkeeper.feature.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class MainActivity : ComponentActivity() {
    // NavController를 handleIntent에서도 접근할 수 있도록 늦은 초기화
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodKeeperTheme {
                navController = rememberNavController() // NavController 생성

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FoodKeeperNavHost(navController)
                }

                // 앱이 처음 켜질 때 알림 인텐트가 있는지 확인
                LaunchedEffect(Unit) {
                    handleIntent(intent)
                }
            }
        }
    }
    // ✅ 앱이 켜져 있는 상태에서 알람을 누르면 이 함수가 실행됩니다!
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 새로운 인텐트로 교체
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra("navigate_to")
        if (target == "home") {
            // 💡 여기서 NavController를 이용해 화면을 전환합니다.
            // 이미 홈이면 아무것도 안 하거나, 홈 탭으로 강제 이동시킵니다.
            Log.d("MainActivity", "알람 클릭으로 홈 이동 처리")
            // "main" 경로로 이동하고, 스택에 쌓인 이전 화면들을 정리
            navController.navigate("main") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

/**
* 앱 전체의 화면 흐름을 관리하는 함수
*/
@Composable
fun FoodKeeperNavHost(navController: NavHostController) {
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
                    navController.navigate("main") {
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
        // 1. 히스토리(목록) 화면
        composable("ai_recipe_history") {        AiRecipeHistoryScreen(
            onRecipeClick = { recipeId ->
                // ✅ 클릭 시 ID를 경로에 담아 이동
                navController.navigate("ai_recipe_detail/$recipeId")
            },
            onBackClick = { navController.popBackStack() }
        )
        }

        // 2. 디테일(상세) 화면
        composable(
            route = "ai_recipe_detail/{recipeId}", // ✅ 인자를 받는 경로
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            AiRecipeDetailScreen(
                onBackClick = { navController.popBackStack() },
                // 필요한 다른 콜백들...
            )
        }
    }
}

