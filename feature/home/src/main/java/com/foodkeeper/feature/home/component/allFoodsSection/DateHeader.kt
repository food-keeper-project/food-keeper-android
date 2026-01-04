package com.foodkeeper.feature.home.component.allFoodsSection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

/**
 * 추가한 날짜 헤더
 */

@Composable
fun DateHeader(date: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = date,
            style = AppFonts.size12Caption1,
            color = AppColors.black
        )

        Spacer(modifier = Modifier.height(2.dp))

        Divider(
            thickness = 1.dp,
            color = AppColors.light3Gray
        )
    }
}
