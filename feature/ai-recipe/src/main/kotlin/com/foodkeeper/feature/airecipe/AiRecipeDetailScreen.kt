package com.foodkeeper.feature.airecipe // 이 줄이 반드시 맨 위에 있어야 합니다.

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun AiRecipeDetailScreen(
    onBackClick: () -> Unit,
    viewModel: AiRecipeDetailViewModel = hiltViewModel()
) {
    // collectAsState 대신 수명 주기를 인식하는 collectAsStateWithLifecycle 권장
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.material3.Scaffold(
        topBar = {
            // ✅ 상단바 및 뒤로가기 버튼 구현
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AI 추천 레시피",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // 1. 레시피 이미지
            item {
                coil.compose.AsyncImage(
                    model = uiState.recipeImage,
                    contentDescription = null,
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            // 2. 제목 & 3. 한줄 설명
            item {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(text = uiState.title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    androidx.compose.material3.Text(text = uiState.description, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.Gray)
                }
            }

            // 4. 예상 소요 시간
            item {
                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Default.Timer, contentDescription = null)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(
                        4.dp
                    ))
                    androidx.compose.material3.Text(text = "요리 예상 소요시간: ${uiState.cookingTime}")
                }
            }

            // 5. 레시피 재료 토글
            item {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.toggleIngredients() },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text(if (uiState.isIngredientsExpanded) "재료 숨기기" else "필요한 재료 보기")
                    }

                    if (uiState.isIngredientsExpanded) {
                        androidx.compose.material3.Card(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(
                                12.dp
                            )) {
                                uiState.ingredients.forEach { (name, amount) ->
                                    androidx.compose.material3.Text("• $name: $amount", modifier = androidx.compose.ui.Modifier.padding(
                                        vertical = 2.dp
                                    ))
                                }
                            }
                        }
                    }
                }
            }

            // 6. 레시피 스텝 설명 (리스트 내 리스트 형태로 구현)
            item {
                androidx.compose.material3.Text(text = "요리 순서", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }

            items(uiState.steps) { step ->
                androidx.compose.material3.Text(
                    text = step,
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }

            // 7. 하단 버튼 (저장하기, 다시 생성하기)
            item {
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.generateRecipe() },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Text("다시 생성")
                    }
                    androidx.compose.material3.Button(
                        onClick = { viewModel.saveRecipe() },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Text("레시피 저장")
                    }
                }
            }
        }
    }
}
