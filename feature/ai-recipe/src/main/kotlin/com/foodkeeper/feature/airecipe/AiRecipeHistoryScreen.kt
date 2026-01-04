package com.foodkeeper.feature.airecipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // ✅ items 대신 itemsIndexed 임포트
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecipeHistoryScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: AiRecipeHistoryViewModel = hiltViewModel()
) {


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // ✅ 화면이 다시 보일 때마다(탭 클릭, 뒤로가기 포함) 리스트 첫 페이지를 새로 가져옴
            viewModel.fetchSavedRecipes(isFirstPage = true)
        }
    }
    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내가 저장한 AI 레시피 목록",
                        style = AppFonts.size19Title3,
                        fontWeight = FontWeight.Bold,
                        color= AppColors.black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.white
                )
            )
        }
    ) { padding ->
        // 1. 초기 로딩 상태
        if (uiState.isLoading && uiState.savedRecipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.main)
            }
        }
        // 2. 데이터가 없을 때 (Empty State) ✅ 추가된 부분
        else if (uiState.savedRecipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.empty_favorite_recipe),
                    contentDescription = "저장된 레시피 없음",
                    modifier = Modifier.size(200.dp)
                )

            }
        }
        // 3. 리스트가 있을 때
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ✅ itemsIndexed를 사용하여 페이징 트리거 감지
                itemsIndexed(uiState.savedRecipes) { index, recipe ->
                    SavedRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) }
                    )

                    // ✅ 마지막 아이템 도달 시 다음 페이지 요청
                    if (index == uiState.savedRecipes.lastIndex && uiState.hasNext && !uiState.isPaging) {
                        LaunchedEffect(Unit) {
                            viewModel.fetchSavedRecipes(isFirstPage = false)
                        }
                    }
                }

                // 페이징 로딩 바
                if (uiState.isPaging) {
                    item {
                        Box(Modifier
                            .fillMaxWidth()
                            .padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AppColors.main)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedRecipeCard(
    recipe: AiRecipeItemState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .height(IntrinsicSize.Min) // 고정 높이 80.dp보다 내용에 맞게 조정하는 것이 안전함
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.title,
                    style = AppFonts.size16Body1,
                    color = AppColors.main,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = recipe.description,
                    style = AppFonts.size12Caption1,
                    color = AppColors.black,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .background(
                            color = AppColors.light6Gray, // 또는 원하는 다크그레이 색상 (ex: Color(0xFF424242))
                            shape = RoundedCornerShape(4.dp) // 모서리를 살짝 둥글게
                        )
                        .padding(horizontal = 2.dp, vertical = 2.dp), // 배경 안쪽 여백
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.history_timer),
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = Color(0xFF1C274C) // ✅ 색상 코드 괄호 및 값 수정 (0xFF... 형식)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "요리 예상 소요시간",
                        style = AppFonts.size10Caption2,
                        color = AppColors.black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.cookMinutes}분",
                        style = AppFonts.size10Caption2,
                        color = AppColors.main,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}
