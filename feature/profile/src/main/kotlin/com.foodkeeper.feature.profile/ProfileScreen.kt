@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.foodkeeper.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    onLogoutSuccess: () -> Unit,
    onWithdrawalClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.logoutSuccess.collectLatest { success ->
            if (success) {
                onLogoutSuccess()
            }
        }
    }

    ProfileScreen(
        profile = profile,
        onLogoutClick = { viewModel.logout() },
        onTermsClick = { /* 이용약관 이동 로직 */ },
        onWithdrawalClick = onWithdrawalClick
    )
}

@Composable
internal fun ProfileScreen(
    profile: ProfileDTO?,
    onLogoutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onWithdrawalClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.white
    ) {
        Scaffold(
            containerColor = AppColors.white
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                // [컬럼 1] 프로필 헤더
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, AppColors.main, CircleShape)
                                .background(AppColors.light3Gray)
                        ) {
                            AsyncImage(
                                model = profile?.imageUrl,
                                contentDescription = "프로필 이미지",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = profile?.nickname?.ifEmpty { "사용자님" } ?: "로그인이 필요합니다",
                            style = AppFonts.size22Title2,
                            color = AppColors.black
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }

                // [컬럼 1-1] 설정 섹션
                item {
                    Text(
                        text = "설정",
                        style = AppFonts.size12Caption1,
                        color = AppColors.light3Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ProfileMenuItem(
                        title = "이용약관 확인하기",
                        onClick = onTermsClick
                    )
                }

                // [컬럼 1-2] 계정 섹션
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "계정",
                        style = AppFonts.size12Caption1,
                        color = AppColors.light3Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ProfileMenuItem(
                        title = "로그아웃",
                        titleColor = AppColors.main,
                        onClick = onLogoutClick
                    )
                    ProfileMenuItem(
                        title = "회원탈퇴",
                        titleColor = AppColors.light3Gray,

                        onClick = onWithdrawalClick
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    titleColor: Color = AppColors.black
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AppFonts.size16Body1,
                color = titleColor
            )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.light3Gray)

        }
    }
}
