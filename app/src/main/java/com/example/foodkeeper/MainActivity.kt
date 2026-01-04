package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import AiRecipeHistoryDetailScreen
import WithdrawalRoute
import android.content.pm.PackageManager
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
import com.foodkeeper.feature.airecipe.AiRecipeHistoryScreen
import com.example.foodkeeper_main.MainScaffoldScreen
import com.example.foodkeeper_main.MainTab
import com.foodkeeper.core.data.network.SessionManager
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.feature.airecipe.AiRecipeGeneratorScreen
import com.foodkeeper.feature.home.HomeScreen
import com.foodkeeper.feature.home.HomeViewModel
import com.foodkeeper.feature.profile.ProfileRoute
import com.foodkeeper.feature.splash.OnboardingScreen
import com.foodkeeper.feature.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 키 해시 추출 코드 (수정본)
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())

                // android.util.Base64를 사용하여 오류 해결
                val keyHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                Log.d("KeyHash", "현재 앱의 키 해시: ${keyHash.trim()}")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "해시 키를 가져올 수 없습니다.", e)
        }
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

        // ✅ 메인 화면 (하단바가 있는 영역)
        composable("main") {
            // 메인 안에서 또 다른 NavHost를 관리하거나
            // 현재 구조를 유지하되 "상태 초기화"를 확실히 해야 함

            // 기존의 수동 화면 전환 방식을 유지하면서 꼬이지 않게 하려면
            // 탭 변경 시 모든 상세 페이지 상태를 초기화해야 합니다.

            var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }
            var selectedRecipeId by rememberSaveable { mutableStateOf(0L) }
            var selectedIngredients by rememberSaveable { mutableStateOf<List<Food>>(emptyList()) }
            var isWithdrawalMode by rememberSaveable { mutableStateOf(false) }
            Log.d("TAG", "FoodKeeperNavHost: selectedRecipeId $selectedRecipeId")
            MainScaffoldScreen(
                currentTab = currentTab,
                showTopBar = (selectedRecipeId == 0L && !isWithdrawalMode), // 상세페이지면 상단바 숨김
                onTabSelected = { tab ->
                    if (tab == MainTab.AddFood) {
                        navController.navigate("addFood")
                    } else {
                        // ✅ 핵심: 탭을 바꿀 때 모든 상세 페이지 상태를 "0" 혹은 "false"로 초기화!
                        // 이렇게 해야 마이페이지 갔다가 홈 눌렀을 때 상세페이지가 안 뜹니다.
                        currentTab = tab
                        selectedRecipeId = 0L
                        isWithdrawalMode = false
                    }
                }
            ) {
                when (currentTab) {
                    MainTab.Home -> {
                        if (selectedRecipeId != 0L) {
                            // 홈에서 생성하기 버튼 누른 경우 -> 생성 화면
                            AiRecipeGeneratorScreen(
                                ingredients = selectedIngredients,
                                onBackClick = { selectedRecipeId = 0L }
                            )
                        } else {
                            HomeScreen(
                                onRecipeRecommendFoods = { ingredients ->
                                    selectedIngredients = ingredients
                                    selectedRecipeId = 1L // 생성 트리거
                                }
                            )
                        }
                    }

                    MainTab.Recipe -> {
                        if (selectedRecipeId != 0L) {// 목록에서 클릭해서 들어온 경우 -> 히스토리 상세 화면
                            AiRecipeHistoryDetailScreen(
                                recipeId = selectedRecipeId,
                                onBackClick = { selectedRecipeId = 0L }
                            )
                        } else {
                            AiRecipeHistoryScreen(
                                onRecipeClick = { id -> selectedRecipeId = id }
                            )
                        }
                    }

                    MainTab.MyPage -> {
                        if (isWithdrawalMode) {
                            WithdrawalRoute(
                                onBackClick = { isWithdrawalMode = false },
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
                                onWithdrawalClick = { isWithdrawalMode = true }
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        // ✅ 메인 바깥 (하단바 없는 전체 화면)
        composable("addFood") {
            AddFoodScreen(onBackClick = { navController.popBackStack() })
        }
//        //프로필 화면
//        composable("profile") {
//            ProfileRoute(
//                onLogoutSuccess = {
//                    navController.navigate("login") {
//                        popUpTo(0) { inclusive = true }
//                    }
//                },
//                onWithdrawalClick = {
//                    // ✅ 회원탈퇴 스크린으로 이동
//                    navController.navigate("withdrawal")
//                }
//            )
//        }
//        //회원탈퇴 화면
//        composable("withdrawal") {
//            // ✅ 회원탈퇴 전용 Route 호출
//            WithdrawalRoute(
//                onBackClick = {
//                    navController.popBackStack() // 뒤로가기
//                },
//                onWithdrawalSuccess = {
//                    // ✅ 탈퇴 성공 시 로그인 화면으로 이동
//                    navController.navigate("login") {
//                        popUpTo(0) { inclusive = true }
//                    }
//                }
//            )
//        }
//        //레시피 목록 화면
//        composable("ai_recipe_history") {
//            AiRecipeHistoryScreen(
//                onRecipeClick = { recipeId ->
//                    navController.navigate("ai_recipe_detail/$recipeId")
//                }
//            )
//        }

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
