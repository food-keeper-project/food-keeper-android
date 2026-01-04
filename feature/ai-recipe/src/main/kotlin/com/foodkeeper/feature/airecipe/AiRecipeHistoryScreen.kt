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
        // ✅ Box로 감싸서 Scaffold가 주는 padding(상단바 높이)을 적용합니다.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 1. 에러 발생 시 ✅ (추가된 부분)
                uiState.isError -> {
                    AiHistoryErrorScreen(
                        onRetry = { viewModel.fetchSavedRecipes(isFirstPage = true) }
                    )
                }

                // 2. 초기 로딩 상태
                uiState.isLoading && uiState.savedRecipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.main)
                    }
                }

                // 3. 데이터가 없을 때 (Empty State)
                uiState.savedRecipes.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_favorite_recipe),
                            contentDescription = "저장된 레시피 없음",
                            modifier = Modifier.size(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "저장된 레시피가 없어요",
//                        style = AppFonts.size16Body1,
//                        color = AppColors.light3Gray
//                    )
                    }
                }

                // 4. 리스트 출력
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(uiState.savedRecipes) { index, recipe ->
                            SavedRecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) }
                            )

                            if (index == uiState.savedRecipes.lastIndex && uiState.hasNext && !uiState.isPaging) {
                                LaunchedEffect(Unit) {
                                    viewModel.fetchSavedRecipes(isFirstPage = false)
                                }
                            }
                        }

                        if (uiState.isPaging) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AppColors.main
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
/**
 * 서버 오류 시 보여줄 에러 화면 ✅
 */
@Composable
fun AiHistoryErrorScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.history_timer), // 적절한 경고 아이콘으로 교체 가능
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.light4Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "서버 오류가 발생하였습니다.",
            style = AppFonts.size16Body1,
            fontWeight = FontWeight.Bold,
            color = AppColors.black
        )
        Text(
            text = "잠시 후 다시 시도해주세요.",
            style = AppFonts.size14Body2,
            color = AppColors.light2Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.main),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "다시 시도하기", color = AppColors.white)
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
