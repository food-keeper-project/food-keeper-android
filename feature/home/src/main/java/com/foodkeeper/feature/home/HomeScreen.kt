package com.foodkeeper.feature.home

import FoodDetailDialog
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodkeeper.core.R
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.base.BaseUiState
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddString
import com.foodkeeper.feature.home.component.allFoodsSection.CategoryTabs
import com.foodkeeper.feature.home.component.allFoodsSection.DateHeader
import com.foodkeeper.feature.home.component.allFoodsSection.FoodListItem
import com.foodkeeper.feature.home.component.dialog.RecipeRecommendationDialog
import com.foodkeeper.feature.home.component.dialog.findActivity
import com.foodkeeper.feature.home.component.dialog.showGoToSettingsDialog
import com.foodkeeper.feature.home.component.dialog.showNotificationRationaleDialog
import com.foodkeeper.feature.home.component.expiringFoodsSection.ExpiringFoodsSection
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 메인 홈 화면
 * 유통기한 임박 식품 + 나의 식재료 리스트
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onRecipeRecommendFoods: (List<Food>) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var isFirstPermissionRequest by rememberSaveable { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isFirstPermissionRequest = false
        // 권한 요청 후 상태 업데이트가 필요하다면 코드 작성 필요
    }

    val uiState by viewModel.uiState.collectAsState()
    val expiringFoodList by viewModel.expiringFoodList.collectAsState()
    val foodCategorys by viewModel.foodCategories.collectAsState()
    val foodList by viewModel.foodList.collectAsState()
    val selectedFood by viewModel.selectedFood.collectAsState()
    val selectedRecipeRecommend by viewModel.selectedRecipeRecommend.collectAsState()

    LaunchedEffect(Unit) {
        // 토스트 메시지 수집
        launch {
            viewModel.toastMessage.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // 권한 이벤트 수집
        launch {
            viewModel.permissionEvent.collect { event ->
                when (event) {
                    NotificationPermissionEvent.RequestPermission -> {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    NotificationPermissionEvent.ShowRationale -> {
                        showNotificationRationaleDialog(context) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    NotificationPermissionEvent.GoToSettings -> {
                        showGoToSettingsDialog(context)
                    }
                }
            }
        }

        // 데이터 로딩
        viewModel.onScreenEnter()

        // 권한 체크 실행 (안드로이드 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity != null) {
            val isGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS
            )

            viewModel.checkNotificationPermission(
                isGranted = isGranted,
                shouldShowRationale = shouldShowRationale,
                isFirstRequest = isFirstPermissionRequest
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.white)) {
        when (uiState) {
            is BaseUiState.Init -> { /* 초기화 UI */ }
            is BaseUiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9500))
                }
            }
            is BaseUiState.Content, is BaseUiState.Processing -> {
                HomeContent(
                    expiringFoodList = expiringFoodList,
                    foodCategorys = foodCategorys,
                    foodList = foodList,
                    onFoodItemClick = viewModel::onFoodItemClick
                )
                FloatingActionButton(
                    onClick = { viewModel.onRecipeRecommendClick(foodList) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp).size(60.dp),
                    shape = CircleShape,
                    containerColor = AppColors.main
                ) {
                    Text("AI", style = AppFonts.size22Title2, color = AppColors.white)
                }
            }
            is BaseUiState.ErrorState -> {
                HomeErrorScreen(onRetry = viewModel::onScreenEnter)
            }
        }

        if (uiState is BaseUiState.Processing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = true, indication = null, interactionSource = remember { MutableInteractionSource() }) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9500))
            }
        }
    }

    // 다이얼로그 처리
    selectedFood?.let { food ->
        FoodDetailDialog(
            food = food,
            categorys = foodCategorys,
            onDismiss = viewModel::onDismissDialog,
            onConsumption = viewModel::onConsumptionFood,
            onUpdate = viewModel::onUpdateFood
        )
    }
    selectedRecipeRecommend?.let { list ->
        RecipeRecommendationDialog(
            list.sortedBy { it.expiryDate.getDDay() },
            onDismiss = viewModel::onDismissDialog,
            onGenerateRecipe = { selectFoods ->
                onRecipeRecommendFoods(selectFoods)
                viewModel.onDismissDialog()
            }
        )
    }
}

@Composable
fun HomeErrorScreen(
    onRetry: () -> Unit,modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.white)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 에러를 시각적으로 보여줄 아이콘 (AiHistory와 통일감 유지)
        Icon(
            painter = painterResource(id = R.drawable.foodplaceholder),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = Color.Unspecified // ✅ 이 설정을 하면 이미지 본래 색상이 나옵니다.
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "데이터를 불러오지 못했습니다",
            style = AppFonts.size19Title3,
            fontWeight = FontWeight.Bold,
            color = AppColors.black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "서버 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.",
            style = AppFonts.size14Body2,
            color = AppColors.light2Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 다시 시도 버튼
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.main,
                contentColor = AppColors.white
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "다시 시도하기",
                style = AppFonts.size16Body1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeContent(
    expiringFoodList: List<Food>,
    foodCategorys: List<Category>,
    foodList: List<Food>,
    onFoodItemClick: (Food) -> Unit
) {
    val tabs = listOf("전체") + foodCategorys.map { it.name }
    var selectedTab by remember { mutableStateOf("전체") }

    val filteredFoodList = remember(foodList, selectedTab) {
        if (selectedTab == "전체") foodList
        else foodList.filter { it.category == selectedTab }
    }

    val groupedItems = remember(filteredFoodList) {
        filteredFoodList.groupBy { it.createdAt.toyyMMddString() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white)
    ) {

        // 1. 유통기한 임박 식품
        if (expiringFoodList.isNotEmpty()) {
            item {
                ExpiringFoodsSection(
                    expiringCount = expiringFoodList.size,
                    foodItems = expiringFoodList,
                    onFoodItemClick = onFoodItemClick
                )
            }

            // ✅ Spacer를 별도의 item으로 완전히 분리
            // 임박 식품 리스트가 있을 때만 이 30.dp 공간이 생성됩니다.
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // 2. 타이틀
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "나의 식재료 리스트",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.black
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 3. 카테고리 탭 (Sticky)
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.white)
            ) {
                CategoryTabs(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 4. 식재료 리스트
        if (filteredFoodList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // 가로를 꽉 채워야 중앙 정렬이 보입니다.
                        .padding(vertical = 60.dp), // 상하 여백을 충분히 주어 시원하게 보이게 합니다.
                    verticalArrangement = Arrangement.Center, // 세로 방향 중앙 정렬
                    horizontalAlignment = Alignment.CenterHorizontally // 가로 방향 중앙 정렬 (핵심!)
                ) {
                    // 1. 이미지 (Icon 대신 Image 권장하지만, 색상 유지를 위해 Unspecified 사용 유지)
                    Icon(
                        painter = painterResource(id = R.drawable.foodplaceholder),
                        contentDescription = null,
                        modifier = Modifier.size(160.dp),
                        tint = Color.Unspecified
                    )

                    // 3. 텍스트
                    Text(
                        text = "식재료를 등록해보세요!",
                        style = AppFonts.size22Title2,
                        color = AppColors.light4Gray
                    )
                }
            }
        } else {
            groupedItems.forEach { (date, foodsInSection) ->
                item(key = "header_$date") {
                    DateHeader(date)
                }

                items(
                    items = foodsInSection,
                    key = { "${it.id}_${it.createdAt.time}" }
                ) { item ->
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        FoodListItem(
                            item = item,
                            onClick = {
                                onFoodItemClick(item)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


