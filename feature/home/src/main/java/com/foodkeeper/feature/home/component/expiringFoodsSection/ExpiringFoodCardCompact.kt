package com.foodkeeper.feature.home.component.expiringFoodsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import com.foodkeeper.feature.home.component.badges.CompactDDayBadge

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
                style = AppFonts.size16Body1,
                color = Color(0xFF333333),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            // D-Day
            CompactDDayBadge(dDay = item.expiryDate.getDDay())
        }
    }
}
