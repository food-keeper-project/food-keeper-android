package com.foodkeeper.feature.home.component.expiringFoodsSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

/**
 * 유통기한 임박 섹션 헤더
 * "유통기한 임박 식품이 현재 N개 있습니다"
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
                style = AppFonts.size22Title2,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "현재 ",
                    color = Color.White,
                    style = AppFonts.size22Title2,
                )
                Surface(
                    color = AppColors.white, // 배경색: 흰색
                    shape = RoundedCornerShape(6.dp), // 모양: 둥근 모서리 (반경 조절 가능)
                ) {
                    Text(
                        text = "${count}개",
                        color = AppColors.main,
                        style = AppFonts.size22Title2,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = " 있습니다",
                    color = Color.White,
                    style = AppFonts.size22Title2,
                )
            }
        }
    }
}