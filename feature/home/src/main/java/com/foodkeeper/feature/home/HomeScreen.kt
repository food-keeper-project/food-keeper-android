package com.foodkeeper.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.base.BaseUiState
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.toyyMMddString
import com.foodkeeper.feature.home.component.allFoodsSection.CategoryTabs
import com.foodkeeper.feature.home.component.allFoodsSection.DateHeader
import com.foodkeeper.feature.home.component.allFoodsSection.FoodListItem
import com.foodkeeper.feature.home.component.expiringFoodsSection.ExpiringFoodsSection
import kotlinx.coroutines.flow.MutableSharedFlow


/**
 * 메인 홈 화면
 * 유통기한 임박 식품 + 나의 식재료 리스트
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    // ViewModel Input 생성
    val screenEnterFlow = remember {
        MutableSharedFlow<Unit>(replay = 1).apply {
            tryEmit(Unit) // 초기 화면 진입 이벤트
        }
    }

    // ViewModel Transform 호출
    val output = remember {
        viewModel.transform(
            HomeViewModel.Input(
                screenEnter = screenEnterFlow
            )
        )
    }

    // Output State 수집
    val uiState by output.uiState.collectAsState()
    val expiringFoodList by output.expiringFoodList.collectAsState()
    val foodCategorys by output.foodCategorys.collectAsState()
    val foodList by output.foodList.collectAsState()


    // LaunchedEffect로 화면 진입 이벤트 발생
    LaunchedEffect(Unit) {
        screenEnterFlow.emit(Unit)
    }

    // UI State에 따른 화면 분기
    when (uiState) {
        is BaseUiState.Init -> {
            // 초기 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("초기화 중...")
            }
        }

        is BaseUiState.Loading -> {
            // 로딩 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF9500)
                )
            }
        }

        is BaseUiState.Content -> {
            // 컨텐츠 표시
            HomeContent(
                expiringFoodList = expiringFoodList,
                foodCategorys = foodCategorys,
                foodList = foodList,
            )
        }

        is BaseUiState.ErrorState -> {
            // 에러 상태
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
                    Button(
                        onClick = {
                            // 재시도
                            screenEnterFlow.tryEmit(Unit)
                        }
                    ) {
                        Text("다시 시도")
                    }
                }
            }
        }
    }
}

/**
 * 홈 화면 컨텐츠
 * 실제 데이터를 표시하는 컴포저블
 */
@Composable
private fun HomeContent(
    expiringFoodList: List<Food>,
    foodCategorys: List<String>,
    foodList: List<Food>
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
            .background(Color.White)
    ) {
        // 1. 유통기한 임박 식품 섹션
        item {
            ExpiringFoodsSection(
                expiringCount = expiringFoodList.size,
                foodItems = expiringFoodList
            )
        }

        // 2. 나의 식재료 리스트 타이틀
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
                    color = AppColors.text
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // ✨ 3. Sticky Header - 카테고리 탭
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)

            ) {
                CategoryTabs(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
        // 여백
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
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
                    key = { it.id }
                ) { item ->
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        FoodListItem(item = item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


