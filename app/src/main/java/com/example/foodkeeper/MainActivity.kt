package com.example.foodkeeper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import com.foodkeeper.feature.kakaologin.LoginScreen
import com.example.foodkeeper.ui.theme.FoodKeeperTheme
import com.foodkeeper.feature.airecipe.AiRecipeDetailScreen
import com.foodkeeper.feature.airecipe.AiRecipeHistoryScreen
import com.example.foodkeeper_main.MainScaffoldScreen
import com.example.foodkeeper_main.MainTab
import com.foodkeeper.core.data.network.SessionManager
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.feature.home.HomeScreen
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
                    navController.navigate("main") {
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

        composable("main") {
            var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }

            MainScaffoldScreen(
                currentTab = currentTab,
                onTabSelected = { tab -> currentTab = tab }
            ) {
                when (currentTab) {
                    MainTab.Home -> HomeScreen()
                    MainTab.Search -> {}
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

        composable(
            route = "ai_recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            AiRecipeDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
