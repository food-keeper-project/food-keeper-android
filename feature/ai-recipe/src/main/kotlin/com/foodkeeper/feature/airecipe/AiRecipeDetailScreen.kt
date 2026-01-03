package com.foodkeeper.feature.airecipe // 이 줄이 반드시 맨 위에 있어야 합니다.

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun AiRecipeDetailScreen(
    ingredients: List<Food>,
    onBackClick: () -> Unit,
    isFromHistory: Boolean = false, // ✅ 추가: 히스토리에서 왔는지 여부
    viewModel: AiRecipeDetailViewModel = hiltViewModel()
) {
    // collectAsState 대신 수명 주기를 인식하는 collectAsStateWithLifecycle 권장
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// 화면이 처음 켜질 때 딱 한 번 실행
    LaunchedEffect(Unit) {
        if (!isFromHistory) {
            viewModel.generateRecipe(ingredients) // 생성 요청
        } else {
            viewModel.fetchRecipeDetail() // 기존 상세 조회
        }
    }

//    if (uiState.isLoading) {
//        LoadingScreen() // 여기서 로딩 이미지/애니메이션 표시
//    } else {
//        RecipeContent(uiState.recipeData) // 결과 표시
//    }
    androidx.compose.material3.Scaffold(
        topBar = {
            // ✅ 상단바 및 뒤로가기 버튼 구현
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "레시피",
                        style = AppFonts.size22Title2,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // ✅ 로딩 중일 때는 하단 버튼 바를 아예 숨깁니다.
            if (!uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 저장하기 버튼
                    Button(
                        onClick = {
                            viewModel.toggleSaveState()

                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isSaved) AppColors.point else AppColors.light5Gray,
                            contentColor = if (uiState.isSaved) AppColors.white else AppColors.light3Gray
                        )
                    ) {
                        // ✅ 저장 중이면 텍스트 대신 작은 로딩 표시를 하거나 텍스트만 유지
                        if (uiState.isSaving) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppColors.white,
                            strokeWidth = 2.dp
                        )
                    } else{
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center){
                            Text(
                                text = "레시피 저장하기",
                                style = AppFonts.size16Body1,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Image(
                                painter = painterResource(
                                    id = if (uiState.isSaved) R.drawable.filled_heart else R.drawable.empty_heart
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    }

                    // 다시 생성하기 버튼
                    if (!isFromHistory) {
                        Button(
                            onClick = { viewModel.generateRecipe(ingredients) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.main,
                                contentColor = AppColors.white
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "다시 생성하기",
                                    style = AppFonts.size16Body1,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.refresh_icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->

            // ✅ 컨텐츠 영역 분기
            if (uiState.isLoading) {
                // ✅ 로딩 중: 상단바/하단바 사이 공간에 ai_loading 이미지만 표시
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ai_loading),
                        contentDescription = "로딩 이미지",
                        modifier = Modifier.fillMaxSize() // 필요에 따라 크기 조절 (.size(200.dp) 등)
                    )
                }
            }
            else{
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                // 2. 제목 & 3. 한줄 설명
                item {
                    androidx.compose.foundation.layout.Column {
                        Row{

                            Icon(
                                painter = painterResource(id = R.drawable.detail_cook), // 본인 폴더명에 맞게 수정
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = AppColors.main
                            )
                            Text(text = uiState.title, style = AppFonts.size22Title2,color=AppColors.text)

                        }

                        Text(text = uiState.description, style = AppFonts.size14Body2, color = AppColors.text)
                    }
                }

                // 4. 예상 소요 시간
                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.history_timer), // 본인 폴더명에 맞게 수정
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = AppColors.text
                        )
                        Text(text = "요리 예상 소요시간: ",color= AppColors.text, style = AppFonts.size12Caption1)
                        Text(text = "${uiState.cookMinutes}", color = AppColors.main)
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

                    // 6. 요리 순서
                    items(uiState.steps) { step ->
                        // ✅ 수정: uiState.steps가 List<Step> 객체 리스트라면
                        // step.content와 같이 내부 필드에 접근해야 합니다.
                        Text(
                            text = "${step.title ?: ""}\n${step.content ?: ""}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            style = AppFonts.size14Body2
                        )
                    }
            }



        }

    }
}

