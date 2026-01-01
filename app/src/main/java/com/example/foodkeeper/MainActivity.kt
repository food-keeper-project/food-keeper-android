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
import com.example.add_food.AddFoodScreen
import com.foodkeeper.feature.kakaologin.LoginScreen
import com.example.foodkeeper.ui.theme.FoodKeeperTheme
import com.foodkeeper.feature.airecipe.AiRecipeDetailScreen
import com.foodkeeper.feature.airecipe.AiRecipeHistoryScreen
import com.example.foodkeeper_main.MainScaffoldScreen
import com.example.foodkeeper_main.MainTab
import com.foodkeeper.core.data.network.SessionManager
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.feature.home.HomeScreen
import com.foodkeeper.feature.home.HomeViewModel
import com.foodkeeper.feature.profile.ProfileRoute
import com.foodkeeper.feature.splash.OnboardingScreen
import com.foodkeeper.feature.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodKeeperTheme {
                navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.white
                ) {
                    FoodKeeperNavHost(navController)
                }

                // ✅ 네트워크 단에서 발생한 로그아웃 신호 감지
                LaunchedEffect(Unit) {
                    SessionManager.logoutEvent.collect {
                        Log.d("TAG", "MainActivity: 로그아웃 감지 -> 로그인 화면으로 이동")
                        // 불필요한 null 체크 제거 후 즉시 이동
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            } // FoodKeeperTheme 끝
        } // setContent 끝
    } // onCreate 끝
}

/**
 * 앱 전체의 화면 흐름을 관리하는 함수
 */
@Composable
fun FoodKeeperNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
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
                    navController.navigate("profile") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

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
                    // ✨ Search 탭 클릭 시 다른 화면으로 이동
                    if (tab == MainTab.AddFood) {
                        navController.navigate("addFood")
                        return@MainScaffoldScreen  // 탭 변경 없이 종료
                    }
                    currentTab = tab
                }
            ) {
                when (currentTab) {
                    MainTab.Home -> HomeScreen(
                        onRecipeRecommendFoods = {} //레시피 생성 시 필요한 재료들 방출

                    ) //홈
                    MainTab.AddFood -> {}
                    MainTab.Recipe -> AiRecipeHistoryScreen(
                        onRecipeClick = { recipeId ->
                            navController.navigate("ai_recipe_detail/$recipeId")
                        }
                    )
                    MainTab.MyPage -> ProfileRoute(
                        onNavigateToHistory = {},
                        onLogoutSuccess = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        composable("profile") {
            ProfileRoute(
                onNavigateToHistory = {},
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("ai_recipe_history") {
            AiRecipeHistoryScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate("ai_recipe_detail/$recipeId")
                }
            )
        }

        // 2. 디테일(상세) 화면
        composable(
            route = "ai_recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            AiRecipeDetailScreen(
                onBackClick = { navController.popBackStack() },
                // 필요한 다른 콜백들...
            )
        }
        // ✨ 4. 식재료 추가 화면 (새로 추가)
        composable("addFood") {
            AddFoodScreen()
        }
    }
}
