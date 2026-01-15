package com.swyp.foodkeeper.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. 디폴트 다크 컬러 스키마
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// 2. 디폴트 라이트 컬러 스키마 (표준 값으로 복구)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* 다른 기본값 오버라이드 예시
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FoodKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 안드로이드 12+에서 기본적으로 true로 설정하는 것이 디폴트입니다.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 상태바 컬러 설정 (Scaffold의 topBar 영역과 일치시키기)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // ✅ 1. 상태바 배경색을 투명하게 설정 (Scaffold 배경색이 보이도록 함)
            // 또는 android.graphics.Color.WHITE로 고정 가능
            window.statusBarColor = Color.TRANSPARENT

            // ✅ 2. 상태바 아이콘 색상 설정
            // 배경이 흰색일 때 아이콘이 보여야 하므로,
            // 다크모드가 아닐 때(!darkTheme) 아이콘을 어둡게(LightStatusBars) 설정합니다.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            // ✅ 3. (선택사항) 상태바 영역까지 화면이 꽉 차게 그리려면 아래 코드 추가
            // WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

