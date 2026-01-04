package com.foodkeeper.feature.home

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.base.BaseUiState
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddString
import com.foodkeeper.feature.home.component.allFoodsSection.CategoryTabs
import com.foodkeeper.feature.home.component.allFoodsSection.DateHeader
import com.foodkeeper.feature.home.component.allFoodsSection.FoodListItem
import com.foodkeeper.feature.home.component.dialog.FoodDetailDialog
import com.foodkeeper.feature.home.component.dialog.RecipeRecommendationDialog
import com.foodkeeper.feature.home.component.expiringFoodsSection.ExpiringFoodsSection

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

    val uiState by viewModel.uiState.collectAsState()
    val expiringFoodList by viewModel.expiringFoodList.collectAsState()
    val foodCategorys by viewModel.foodCategories.collectAsState()
    val foodList by viewModel.foodList.collectAsState()
    val selectedFood by viewModel.selectedFood.collectAsState()
    val selectedRecipeRecommend by viewModel.selectedRecipeRecommend.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenEnter()
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(AppColors.white)
    ) {

        // --------------------
        // 메인 UI
        // --------------------
        when (uiState) {
            is BaseUiState.Init -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("초기화 중...")
                }
            }

            is BaseUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF9500))
                }
            }

            is BaseUiState.Content,
            is BaseUiState.Processing -> {
                HomeContent(
                    expiringFoodList = expiringFoodList,
                    foodCategorys = foodCategorys,
                    foodList = foodList,
                    onFoodItemClick = { food ->
                        viewModel.onFoodItemClick(food)
                    }
                )
                FloatingActionButton(
                    onClick = {
                        viewModel.onRecipeRecommendClick(foodList)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 30.dp, bottom = 30.dp)
                        .size(60.dp),
                    shape = CircleShape,
                    containerColor = AppColors.main,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "AI",
                        style = AppFonts.size22Title2,
                        color = AppColors.white
                    )
                }
            }

            is BaseUiState.ErrorState -> {
                val error = uiState as BaseUiState.ErrorState
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "오류가 발생했습니다",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = error.message ?: "알 수 없는 오류",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Button(onClick = { viewModel.onScreenEnter() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }

        // --------------------
        // Processing 오버레이
        // --------------------
        if (uiState is BaseUiState.Processing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        enabled = true,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* 아무 것도 안 함 */ },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9500))
            }
        }

    }

    // --------------------
    // 다이얼로그
    // --------------------
    selectedFood?.let { food ->
        FoodDetailDialog(
            food = food,
            onDismiss = viewModel::onDismissDialog,
            onConsumption = viewModel::onConsumptionFood
        )
    }
    selectedRecipeRecommend?.let { foodList ->
        //유통기한 임박 정렬
        val sortedList = foodList.sortedBy { it.expiryDate.getDDay() }//.filter { it.expiryDate.getDDay() >= 0 } //유통기한 지난 식재료 제외 필요 시 주석 해제
        RecipeRecommendationDialog(
            sortedList,
            onDismiss = viewModel::onDismissDialog,
            onGenerateRecipe = { selectFoods ->
                onRecipeRecommendFoods(selectFoods)
                Log.d("TAG", "HomeScreen: ")
                viewModel.onDismissDialog()
            }
        )
    }
}

@Composable
private fun HomeContent(
    expiringFoodList: List<Food>,
    foodCategorys: List<String>,
    foodList: List<Food>,
//    onFoodItemClick: (Food) -> Unit
    onFoodItemClick: (Food) -> Unit
) {
    val tabs = listOf("전체") + foodCategorys
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
        item {
            ExpiringFoodsSection(
                expiringCount = expiringFoodList.size,
                foodItems = expiringFoodList
            )
        }

        // 2. 타이틀
        item {
            Spacer(modifier = Modifier.height(30.dp))
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "식재료가 없습니다",
                        fontSize = 16.sp,
                        color = Color.Gray
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


