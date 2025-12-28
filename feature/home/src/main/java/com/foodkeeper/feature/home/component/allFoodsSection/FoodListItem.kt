package com.foodkeeper.feature.home.component.allFoodsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddString
import com.foodkeeper.feature.home.component.badges.DDayBadge
import com.foodkeeper.feature.home.component.badges.ExpiringBadge

/**
 * 전체 식재료 리스트 아이템
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
                            style = AppFonts.size16Body1,
                            color = AppColors.text
                        )
                        DDayBadge(dDay = item.expiryDate.getDDay())
                        // 7일 미만일 경우 유통기한 임박 뱃지 표시
                        if (item.expiryDate.getDDay() <= 7) {
                            ExpiringBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.category,
                            style = AppFonts.size12Caption1,
                            color = AppColors.grayText
                        )

                        // 텍스트 사이의 구분점(Bullet)을 넣으면 더 깔끔합니다.
                        Text(
                            text = " • ",
                            style = AppFonts.size12Caption1,
                            color = AppColors.grayText
                        )

                        Text(
                            text = "유통기한 ${item.expiryDate.toyyMMddString()}",
                            style = AppFonts.size12Caption1,
                            color = AppColors.text
                        )
                    }
                }
            }
        }
    }
}