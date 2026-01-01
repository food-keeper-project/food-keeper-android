package com.example.foodkeeper_main
// 이 줄을 추가하세요
import com.foodkeeper.feature.foodKeeperMain.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
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
            containerColor = AppColors.white,
            titleContentColor = mMain
        )
    )
}