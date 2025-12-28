package com.foodkeeper.feature.home.component.expiringFoodsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

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
                style = AppFonts.size19Title3,
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


