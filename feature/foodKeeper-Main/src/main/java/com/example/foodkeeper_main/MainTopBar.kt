package com.example.foodkeeper_main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.ui.theme.mMain
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    currentTab: MainTab
) {
    TopAppBar(
        // 🔥 왼쪽 로고
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "로고",
                tint = mMain,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 12.dp)
            )
        },

        // 🔥 타이틀
        title = {
            Text(
                text = AppString.appName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = mMain
        )
    )
}