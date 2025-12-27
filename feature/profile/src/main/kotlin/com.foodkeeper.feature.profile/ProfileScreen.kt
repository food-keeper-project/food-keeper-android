@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.foodkeeper.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.foodkeeper.core.data.mapper.external.ProfileDTO

@Composable
fun ProfileRoute(
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // ViewModel에서 ProfileDTO? 타입을 구독
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    ProfileScreen(
        profile = profile,
        onHistoryClick = onNavigateToHistory,
        onFavoritesClick = onNavigateToFavorites,
        onCategoryClick = onNavigateToCategory,
        onRecipesClick = onNavigateToRecipes
    )
}

@Composable
internal fun ProfileScreen(
    profile: ProfileDTO?,
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onRecipesClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("마이페이지", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // 1. 프로필 정보 섹션
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coil AsyncImage 사용
                    AsyncImage(
                        model = profile?.imageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop,
                        // R.drawable.ic_default_profile가 없다면 프로젝트에 맞는 리소스로 교체 필요
                        //error = painterResource(id =),
                        //placeholder = painterResource(id = com.foodkeeper.core.designsystem.R.drawable.ic_default_profile)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = profile?.nickname?.ifEmpty { "사용자님" } ?: "로그인이 필요합니다",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            // 2. 메뉴 리스트 섹션
            item {
                Text(text = "활동 정보", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { ProfileMenuItem("히스토리", onHistoryClick) }
            item { ProfileMenuItem("즐겨찾기", onFavoritesClick) }
            item { ProfileMenuItem("관심 카테고리 설정", onCategoryClick) }
            item { ProfileMenuItem("나의 레시피", onRecipesClick) }

            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "설정", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { ProfileMenuItem("알림 설정") { /* 알림 설정 이동 */ } }
            item { ProfileMenuItem("로그아웃") { /* ViewModel 로그아웃 호출 */ } }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
}
