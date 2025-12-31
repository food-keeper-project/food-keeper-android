package com.example.foodkeeper_main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.util.AppColors

@Composable
fun MainBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(108.dp), // ✅ 전체 높이 제한
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = currentTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    // ✅ imageVector → painter로 변경
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.main,
                    selectedTextColor = AppColors.main,
                    unselectedIconColor = AppColors.light3Gray,
                    unselectedTextColor = AppColors.light3Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}