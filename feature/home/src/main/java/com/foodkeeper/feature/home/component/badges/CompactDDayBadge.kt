package com.foodkeeper.feature.home.component.badges

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.util.AppFonts

/**
 * 컴팩트 D-Day 뱃지
 */
@Composable
fun CompactDDayBadge(dDay: Int) {
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
        style = AppFonts.size14Body2,
    )
}