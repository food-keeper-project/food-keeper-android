package com.foodkeeper.feature.home.component.badges

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

/**
 * D-Day 뱃지 컴포넌트
 */
@Composable
fun DDayBadge(dDay: Int) {
    val isExpiring = dDay >= 0
    val backgroundColor = if (isExpiring) AppColors.main else AppColors.black
    val textColor = if (isExpiring) Color.White else Color(0xFFFF6D00)

    Surface(
        modifier = Modifier.height(18.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
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
                style = AppFonts.size12Caption1,
                lineHeight = 18.sp,
                maxLines = 1
            )
        }
    }
}