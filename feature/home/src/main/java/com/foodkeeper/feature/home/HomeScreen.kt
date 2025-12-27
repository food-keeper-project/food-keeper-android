package com.foodkeeper.feature.home

import android.R.attr.textColor
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.FoodCategory
import com.foodkeeper.core.ui.base.BaseUiState
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddString
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
    val foodList by output.foodList.collectAsState()
    val expiringFoodList by output.expiringFoodList.collectAsState()

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
    foodList: List<Food>
) {
    val tabs = listOf("전체") + FoodCategory.values().map { it.displayName }
    var selectedTab by remember { mutableStateOf("전체") }

    val filteredFoodList = remember(foodList, selectedTab) {
        if (selectedTab == "전체") foodList
        else foodList.filter { it.category.displayName == selectedTab }
    }
    val groupedItems = remember(filteredFoodList) {
        filteredFoodList.groupBy { it.expiryDate.toyyMMddString() }
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

/**
 * 유통기한 임박 식품 섹션
 * 상단 오렌지 배경의 카드 리스트
 */
@Composable
fun ExpiringFoodsSection(
    expiringCount: Int,
    foodItems: List<Food>
) {
    // 빈 리스트일 경우 처리
    if (foodItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF9500),
                            Color(0xFFFF7A00)
                        )
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "유통기한 임박 식품이 없습니다",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    // 오렌지 그라데이션 배경
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.main)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            //헤더
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ExpiringFoodHeader(count = expiringCount)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✨ 2줄 동시 스크롤 리스트
            TwoRowSyncedList(foodItems = foodItems)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 2줄로 홀짝 배치되는 리스트
 * 첫째 줄: 0, 2, 4, 6...
 * 둘째 줄: 1, 3, 5, 7...
 */
@Composable
fun TwoRowSyncedList(foodItems: List<Food>) {
    val firstRowItems = foodItems.filterIndexed { index, _ -> index % 2 == 0 }
    val secondRowItems = foodItems.filterIndexed { index, _ -> index % 2 == 1 }

    val firstLazyListState = rememberLazyListState()
    val secondLazyListState = rememberLazyListState()

    // ✨ 스크롤 동기화 로직
    LaunchedEffect(firstLazyListState.isScrollInProgress) {
        if (firstLazyListState.isScrollInProgress) {
            snapshotFlow { firstLazyListState.firstVisibleItemIndex to firstLazyListState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    secondLazyListState.scrollToItem(index, offset)
                }
        }
    }

    LaunchedEffect(secondLazyListState.isScrollInProgress) {
        if (secondLazyListState.isScrollInProgress) {
            snapshotFlow { secondLazyListState.firstVisibleItemIndex to secondLazyListState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    firstLazyListState.scrollToItem(index, offset)
                }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 첫째 줄
        LazyRow(
            state = firstLazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(firstRowItems) { item ->
                ExpiringFoodCardCompact(item = item)
            }
        }

        // 둘째 줄
        LazyRow(
            state = secondLazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(secondRowItems) { item ->
                ExpiringFoodCardCompact(item = item)
            }
        }
    }
}
/**
 * 컴팩트한 식품 카드 (2줄 배치용)
 * 이미지와 이름, D-Day를 가로로 배치
 */
@Composable
fun ExpiringFoodCardCompact(
    item: Food
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()  // ✨ 너비 자동 조절
            .height(37.dp),
        shape = RoundedCornerShape(40.dp),  // 더 둥근 모서리
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = AppColors.white,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageURL)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // 이름
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            // D-Day
            DDayBadgeCompact(dDay = item.expiryDate.getDDay())
        }
    }
}


/**
 * 컴팩트 D-Day 뱃지
 */
@Composable
fun DDayBadgeCompact(dDay: Int) {
    val backgroundColor = when {
        dDay <= 3 -> Color(0xFFFF5252) // 빨강
        dDay <= 7 -> Color(0xFFFF9500) // 주황
        else -> Color(0xFFFFE0B2)       // 연한 주황
    }
    val textColor = if (dDay <= 7) Color.White else Color(0xFFFF6D00)

    Text(
        text = "D-$dDay",
        modifier = Modifier.padding(end = 4.dp),
        color = backgroundColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * 유통기한 임박 섹션 헤더
 * "유통기한 임박 식품이 현재 N개 있습니다" + 바로가기 버튼
 */
@Composable
fun ExpiringFoodHeader(
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "유통기한 임박 식품이",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                Text(
                    text = "현재 ",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${count}개",
                    color = AppColors.point,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " 있습니다",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * D-Day 뱃지 컴포넌트
 * 재사용 가능 - 7일 이하면 빨강, 아니면 주황
 */
@Composable
fun DDayBadge(dDay: Int) {
    val isExpiring = dDay >= 0
    val backgroundColor = if (isExpiring) AppColors.main else AppColors.text
    val textColor = if (isExpiring) Color.White else Color(0xFFFF6D00)

    Surface(
        modifier = Modifier.height(18.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpiring) "D-$dDay" else "유통기한 종료",
                color = textColor,
                fontSize = 12.sp,
                lineHeight = 18.sp, // ⭐ 핵심
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

/**
 * 카테고리 탭 컴포넌트
 * 재사용 가능한 탭 필터
 */
@Composable
fun CategoryTabs(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selectedTab
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab,
                        fontSize = 14.sp,
                    )
                },
                border = null,
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.main,
                    selectedLabelColor = AppColors.white,
                    containerColor = AppColors.lightGray,
                    labelColor = AppColors.text
                )
            )
        }
    }
}
@Composable
fun DateHeader(date: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = date,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.text
        )

        Spacer(modifier = Modifier.height(2.dp))

        Divider(
            thickness = 1.dp,
            color = AppColors.lightGray
        )
    }
}

/**
 * 7. 식재료 리스트 아이템
 * 재사용 가능한 리스트 아이템 카드
 */
@Composable
fun FoodListItem(
    item: Food
) {
    val imageShape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // ✅ 테두리(그림자) 제거
        ),
        shape = RoundedCornerShape(0.dp) // (선택) 완전 평면 느낌
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 이미지 + 정보
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이미지 배경
                // 이미지
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = AppColors.white,
                            shape = imageShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageURL)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(imageShape)
                    )
                }

                // 정보
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AppColors.text
                        )
                        DDayBadge(dDay = item.expiryDate.getDDay())
                        if (item.isExpiringSoon) {
                            ExpiringBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${item.category.displayName} • 유통기한 ${item.expiryDate.toyyMMddString()}",
                        fontSize = 12.sp,
                        color = AppColors.lightGray
                    )
                }
            }
        }
    }
}

/**
 * 8. 유통기한 임박 뱃지
 */
@Composable
fun ExpiringBadge() {
    Surface(
        modifier = Modifier.height(18.dp),
        color = AppColors.point,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "유통기한 임박!",
                modifier = Modifier.padding(horizontal = 6.dp),
                color = AppColors.text,
                fontSize = 11.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}