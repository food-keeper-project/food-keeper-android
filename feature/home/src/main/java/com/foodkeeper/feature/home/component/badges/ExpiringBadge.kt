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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

/**
 * 유통기한 임박 뱃지
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
                .padding(horizontal = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "유통기한 임박!",
                modifier = Modifier.padding(horizontal = 6.dp),
                color = AppColors.black,
                style = AppFonts.size12Caption1,
                lineHeight = 18.sp,
            )
        }
    }
}