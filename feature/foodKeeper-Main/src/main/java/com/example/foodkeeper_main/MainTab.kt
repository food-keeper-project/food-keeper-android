package com.example.foodkeeper_main

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.foodkeeper.feature.foodKeeperMain.R

enum class MainTab(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    Home(
        route = "home",
        label = "홈",
        iconRes = R.drawable.home
    ),
    Search(
        route = "add",
        label = "식재료 추가",
        iconRes = R.drawable.cart_plus
    ),
    Recipe(
        route = "recipe",
        label = "AI 레시피",
        iconRes = R.drawable.chef_hat
    ),
    MyPage(
        route = "mypage",
        label = "마이페이지",
        iconRes = R.drawable.user_circle
    )
}