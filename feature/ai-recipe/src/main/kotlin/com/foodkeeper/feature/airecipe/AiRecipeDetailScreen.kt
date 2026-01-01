package com.foodkeeper.feature.airecipe // 이 줄이 반드시 맨 위에 있어야 합니다.

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

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
                        text = "회원탈퇴",
                        style = AppFonts.size22Title2,
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
                }
            )
        },
        // ✅ 1. 버튼을 bottomBar 영역으로 이동합니다.
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // 하단바 여백
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                // 저장하기 버튼
                Button(

                    onClick = { viewModel.saveRecipe()
                              uiState.isSaved.value=!uiState.isSaved.value
                              },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isSaved.value) AppColors.point else AppColors.light5Gray,
                        contentColor = if (uiState.isSaved.value) AppColors.white else AppColors.light3Gray
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center){
                        Text(
                            text = "레시피 저장하기",
                            style = AppFonts.size16Body1,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if (uiState.isSaved.value){
                            Image(
                                painter = painterResource(id = R.drawable.filled_heart),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        else{
                            Image(
                                painter = painterResource(id = R.drawable.empty_heart),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 다시 생성하기 버튼
                Button(
                    onClick = { viewModel.generateRecipe() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.main,
                        contentColor = AppColors.white
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center){
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
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
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
                    Text(text = uiState.cookingTime, color = AppColors.main)
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


        }

    }
}
