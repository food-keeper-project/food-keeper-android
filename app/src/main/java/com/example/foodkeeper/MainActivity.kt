package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.foodkeeper_main.MainScaffoldScreen
import com.example.foodkeeper_main.MainTab
import com.foodkeeper.feature.home.HomeScreen
import com.foodkeeper.feature.home.HomeViewModel
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
                    FoodKeeperNavHost()
                }

                // 앱이 처음 켜질 때 알림 인텐트가 있는지 확인
                LaunchedEffect(Unit) {
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
                    navController.navigate("login") {
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
            var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }

            MainScaffoldScreen(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            ) {
                when (currentTab) {
                    MainTab.Home -> HomeScreen(
                        onRecipeRecommendFoods = {} //레시피 생성 시 필요한 재료들 방출

                    ) //홈
                    MainTab.Search -> HomeScreen(
                        onRecipeRecommendFoods = {} //레시피 생성 시 필요한 재료들 방출
                    ) //식자재 추가
                    MainTab.Record -> HomeScreen(
                        onRecipeRecommendFoods = {} //레시피 생성 시 필요한 재료들 방출
                    ) // AI 레시피
                    MainTab.MyPage -> HomeScreen(
                        onRecipeRecommendFoods = {} //레시피 생성 시 필요한 재료들 방출
                    ) // 마이페이지
                }
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
