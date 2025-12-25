package com.foodkeeper.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.domain.model.Food


/**
 * 메인 홈 화면
 * 유통기한 임박 식품 + 나의 식재료 리스트
 */
@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
        // 1. 유통기한 임박 식품 섹션
        item {
            ExpiringFoodsSection(
                expiringCount = 3,
                foodItems = sampleExpiringFoods,
                onSeeAllClick = { /* 바로가기 */ }
            )
        }

        // 2. 나의 식재료 리스트 섹션
        item {
            Spacer(modifier = Modifier.height(16.dp))
            MyFoodListSection(
                foodItems = sampleMyFoodList
            )
        }
    }
}

/**
 * 1. 유통기한 임박 식품 섹션
 * 상단 오렌지 배경의 카드 리스트
 */
@Composable
fun ExpiringFoodsSection(
    expiringCount: Int,
    foodItems: List<Food>,
    onSeeAllClick: () -> Unit
) {
    // 오렌지 그라데이션 배경
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
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더
            ExpiringFoodHeader(
                count = expiringCount,
                onSeeAllClick = onSeeAllClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 카드 리스트 (가로 스크롤)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(foodItems) { item ->
                    ExpiringFoodCard(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 하단 인디케이터
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * 2. 유통기한 임박 섹션 헤더
 * "유통기한 임박 식품이 현재 N개 있습니다" + 바로가기 버튼
 */
@Composable
fun ExpiringFoodHeader(
    count: Int,
    onSeeAllClick: () -> Unit
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
                    color = Color(0xFFFFEB3B),
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

        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "바로가기",
                color = Color.White,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 3. 유통기한 임박 식품 카드
 * 재사용 가능한 카드 컴포넌트
 */
@Composable
fun ExpiringFoodCard(
    item: Food
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더: 이름 + D-Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                DDayBadge(dDay = item.dDay)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 이미지 (실제로는 이미지 로드)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 60.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 레시피 추천 버튼
            Button(
                onClick = { /* 레시피 추천 */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "레시피 추천",
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * 4. D-Day 뱃지 컴포넌트
 * 재사용 가능 - 7일 이하면 빨강, 아니면 주황
 */
@Composable
fun DDayBadge(dDay: Int) {
    val isExpiring = dDay <= 7
    val backgroundColor = if (isExpiring) Color(0xFFFF9500) else Color(0xFFFFE0B2)
    val textColor = if (isExpiring) Color.White else Color(0xFFFF6D00)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "D-$dDay",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 5. 나의 식재료 리스트 섹션
 */
@Composable
fun MyFoodListSection(
    foodItems: List<Food>
) {
    var selectedTab by remember { mutableStateOf("전체") }
    val tabs = listOf("전체", "야채류", "육류", "해산물", "유제품")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 제목
        Text(
            text = "나의 식재료 리스트",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 카테고리 탭
        CategoryTabs(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜
        Text(
            text = "25. 12. 20",
            fontSize = 14.sp,
            color = Color(0xFF999999)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 식재료 리스트
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            foodItems.forEach { item ->
                FoodListItem(item = item)
            }
        }
    }
}

/**
 * 6. 카테고리 탭 컴포넌트
 * 재사용 가능한 탭 필터
 */
@Composable
fun CategoryTabs(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF9500),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFE0E0E0),
                    labelColor = Color(0xFF666666)
                )
            )
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 이미지 + 정보
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이미지 배경
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = if (item.isExpiringSoon)
                                Color(0xFFFFEB3B)
                            else
                                Color(0xFFFF9500),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = 32.sp
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
                            fontSize = 18.sp,
                            color = Color(0xFF333333)
                        )
                        DDayBadge(dDay = item.dDay)
                        if (item.isExpiringSoon) {
                            ExpiringBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${item.category} • 유통기한 ${item.expiryDate}",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            // 오른쪽: 검색 버튼
            IconButton(
                onClick = { /* 검색 */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color(0xFF666666)
                )
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
        color = Color(0xFFFFEB3B),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "유통기한 임박!",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color(0xFF795548),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}