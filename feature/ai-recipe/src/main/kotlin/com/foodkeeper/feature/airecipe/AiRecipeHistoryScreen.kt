package com.foodkeeper.feature.airecipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecipeHistoryScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: AiRecipeHistoryViewModel = hiltViewModel() // ✅ DetailScreen처럼 ViewModel을 주입받음
) {
    // ✅ ViewModel의 상태를 구독
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = AppColors.white,
        topBar = {

        }
    ) { padding ->
        // 로딩 처리 추가
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "내가 저장한 AI 레시피 목록",
                        style = AppFonts.size19Title3,
                        color=AppColors.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(uiState.savedRecipes) { recipe ->
                    SavedRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) }
                    )
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
                .padding(12.dp)
                .height(80.dp)
        ) {

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.title,
                    style = AppFonts.size16Body1,
                    color= AppColors.main,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = AppFonts.size12Caption1,
                    color = AppColors.text,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    // 변환된 XML 아이콘 사용
                    Icon(
                        painter = painterResource(id = R.drawable.history_timer), // 본인 폴더명에 맞게 수정
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color((0xF1C274C)) // 아이콘 색상 입히기
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "요리 예상 소요시간", // 실제 데이터라면 recipe.cookingTime 등으로 변경
                        style = AppFonts.size10Caption2,
                        color = AppColors.text
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${recipe.cookMinutes}분", // 실제 데이터라면 recipe.cookingTime 등으로 변경
                        style = AppFonts.size10Caption2,
                        color = AppColors.main,
                        fontWeight = FontWeight.ExtraBold
                    )

                }
            }
        }
    }
}
