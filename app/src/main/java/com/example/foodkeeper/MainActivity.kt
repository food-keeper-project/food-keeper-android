package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import WithdrawalRoute
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
import com.foodkeeper.core.domain.model.Food
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
        //온보딩 화면
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
        //스플래쉬 화면
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
        
        //로그인 화면
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
// ✅ 상세 화면 진입 여부를 체크하는 상태값들
            var selectedRecipeId by rememberSaveable { mutableStateOf<Long?>(0L) }
            var isWithdrawalMode by rememberSaveable { mutableStateOf(false) }
// ✅ 1. 추가: 상세 페이지에서 사용할 상태값들
            var selectedIngredients by rememberSaveable { mutableStateOf<List<Food>>(emptyList()) }
            var isFromHistory by rememberSaveable { mutableStateOf(false) }

            // ✅ 하단바는 유지하되, 특정 상황(상세페이지)에서 상단바를 숨김 처리
            val showTopBar = when {
                currentTab == MainTab.Recipe && selectedRecipeId != 0L -> false
                currentTab == MainTab.MyPage && isWithdrawalMode -> false
                else -> true
            }
            MainScaffoldScreen(
                currentTab = currentTab,
                showTopBar = showTopBar,
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
                    MainTab.Home -> {
                        // ✅ 홈 탭에서도 상세 화면 진입 여부를 체크
                        if (selectedRecipeId !=0L && !isFromHistory) {
                            // ✅ 홈에서 재료를 선택해 넘어온 경우 상세 화면 표시
                            AiRecipeDetailScreen(
                                ingredients = selectedIngredients,
                                isFromHistory = false,
                                onBackClick = {
                                    selectedRecipeId = 0L
                                    selectedIngredients = emptyList()
                                }
                            )
                        } else {
                            // ✅ 기본 홈 화면
                            HomeScreen(
                                onRecipeRecommendFoods = { ingredients ->
                                    // 콜백 발생 시 데이터 설정 -> 위 if문이 true가 되어 상세화면으로 교체됨
                                    selectedIngredients = ingredients
                                    isFromHistory = false
                                    selectedRecipeId = 1L // 트리거 역할
                                }
                            )
                        }
                    } // 홈 끝
                    MainTab.AddFood -> {}
                    MainTab.Recipe -> {
                        // 상세 화면으로 갈 레시피 ID를 저장하는 상태 (null이면 목록, 아니면 상세)

                        if (selectedRecipeId !=0L) {
                            // ✅ 레시피 상세 화면 (하단바 유지됨)
                            AiRecipeDetailScreen(
                                // 홈에서 올 때는 실제 리스트, 히스토리에서 올 때는 빈 리스트 전달
                                ingredients = emptyList(),
                                isFromHistory =  true,
                                onBackClick = { selectedRecipeId = 0L
                                } // 뒤로가기 시 다시 목록으로
                                // 필요한 다른 파라미터가 있다면 여기에 추가
                            )
                        } else {
                            // ✅ 레시피 목록 화면
                            AiRecipeHistoryScreen(
                                onRecipeClick = { recipeId ->
                                    // 클릭 시 상태를 업데이트하여 상세 화면으로 전환
                                    selectedRecipeId = recipeId
                                }
                            )
                        }
                    }

                    MainTab.MyPage -> {
                        // 내부에서 탈퇴 화면 여부를 관리하는 상태 (또는 ProfileRoute 내부에서 관리 가능)

                        if (isWithdrawalMode) {
                            WithdrawalRoute(
                                onBackClick = { isWithdrawalMode = false }, // 뒤로가기 시 다시 프로필로
                                onWithdrawalSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            ProfileRoute(
                                onLogoutSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onWithdrawalClick = {
                                    isWithdrawalMode = true // ✅ 하단바를 유지한 채 화면만 교체
                                }
                            )
                        }
                    }

                }
            }
        }
        //프로필 화면
        composable("profile") {
            ProfileRoute(
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onWithdrawalClick = {
                    // ✅ 회원탈퇴 스크린으로 이동
                    navController.navigate("withdrawal")
                }
            )
        }
        //회원탈퇴 화면
        composable("withdrawal") {
            // ✅ 회원탈퇴 전용 Route 호출
            WithdrawalRoute(
                onBackClick = {
                    navController.popBackStack() // 뒤로가기
                },
                onWithdrawalSuccess = {
                    // ✅ 탈퇴 성공 시 로그인 화면으로 이동
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        //레시피 목록 화면
        composable("ai_recipe_history") {
            AiRecipeHistoryScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate("ai_recipe_detail/$recipeId")
                }
            )
        }

//        // 레시피 디테일(상세) 화면
//        composable(
//            route = "ai_recipe_detail/{recipeId}",
//            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
//        ) {
//            AiRecipeDetailScreen(
//                onBackClick = { navController.popBackStack() },
//                // 필요한 다른 콜백들...
//            )
//        }
        // ✨ 4. 식재료 추가 화면 (새로 추가)
        composable("addFood") {
            AddFoodScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
