// feature/splash/src/main/kotlin/com/foodkeeper/feature/splash/SplashScreen.kt
package com.foodkeeper.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.AppString

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    // destination 상태가 변경될 때마다 화면 이동 처리
    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Onboarding -> onNavigateToOnboarding()
            is SplashDestination.Login -> onNavigateToLogin()
            is SplashDestination.Main -> onNavigateToMain()
            null -> { /* 아직 로딩 중 */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = AppString.appName,
                style = AppFonts.size50Title0,
                color = AppColors.main
            )
        }
    }
}
