package com.foodkeeper.feature.airecipe // 이 줄이 반드시 맨 위에 있어야 합니다.

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecipeDetailScreen(
    ingredients: List<Food>,
    onBackClick: () -> Unit,
    isFromHistory: Boolean = false,
    viewModel: AiRecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// ✅ 모달 상태 관리용 변수 추가
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 화면 진입 시 초기 데이터 로드
    LaunchedEffect(Unit) {
        if (!isFromHistory) {
            viewModel.generateRecipe(ingredients)
        } else {
            viewModel.fetchRecipeDetail()
        }
    }// ✅ 화면을 떠날 때(다른 탭 이동 포함) 데이터를 비우도록 명령
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAllState() // ViewModel에 public으로 열어준 뒤 호출
        }
    }

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.white // 배경색 유지 필요 시 추가
                )
            )
        },
        bottomBar = {
            // ✅ 로딩 중이 아닐 때만 버튼 노출 (이전 레시피 버튼 잔상 방지)
            if (!uiState.isLoading && uiState.title.isNotEmpty()) {
                AiRecipeBottomBar(
                    uiState = uiState,
                    isFromHistory = isFromHistory,
                    ingredients = ingredients,
                    viewModel = viewModel,
                    onRegenerateClick = { showConfirmDialog = true }
                )
            }
        }
    ) { padding ->
        // ✅ 배경색 통일을 위해 Surface로 감싸거나 Modifier.background 사용
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = AppColors.white
        ) {
            if (uiState.isLoading || uiState.title.isEmpty()) {
                AiLoadingScreen()
            } else {
                RecipeDetailContent(uiState, viewModel)
            }
        }
    }
    // ✅ 모달 노출 로직 추가
    if (showConfirmDialog) {
        RecipeConfirmDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = { viewModel.generateRecipe(ingredients) }
        )
    }
}

@Composable
fun AiLoadingScreen() {
    // 전체 배경은 흰색
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ 이미지: fillMaxSize를 제거하고, 이미지 고유 크기를 유지하거나 wrapContentSize로 설정
            Image(
                painter = painterResource(id = R.drawable.ai_loading),
                contentDescription = "로딩 이미지",
                modifier = Modifier
                    .height(280.dp)
                    .width(170.dp) // 너무 크면 화면 밖으로 나가므로 적절한 너비 지정 (또는 wrapContentSize)
                    .clip(RoundedCornerShape(12.dp))
            )

            // ✅ 이미지와 텍스트 사이 간격
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI가 레시피를 생성중입니다...",
                style = AppFonts.size16Body1,
                color = AppColors.main,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecipeDetailContent(uiState: AiRecipeUiState, viewModel: AiRecipeDetailViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(AppColors.white),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.detail_cook),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = AppColors.main
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = uiState.title, style = AppFonts.size22Title2, color = AppColors.text)
                }
                Text(text = uiState.description, style = AppFonts.size14Body2, color = AppColors.text)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.history_timer),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppColors.text
                )
                Text(text = "요리 예상 소요시간: ", color = AppColors.text, style = AppFonts.size12Caption1)
                Text(text = "${uiState.cookMinutes}분", color = AppColors.main, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Column {
                Button(
                    onClick = { viewModel.toggleIngredients() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.light5Gray, contentColor = AppColors.text)
                ) {
                    Text(if (uiState.isIngredientsExpanded) "재료 숨기기" else "필요한 재료 보기")
                }

                if (uiState.isIngredientsExpanded) {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = AppColors.light5Gray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            uiState.ingredients.forEach { (name, amount) ->
                                Text("• $name: $amount", style = AppFonts.size14Body2, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(text = "요리 순서", style = AppFonts.size19Title3, fontWeight = FontWeight.Bold, color = AppColors.text)
        }

        items(uiState.steps) { step ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
                if (!step.title.isNullOrEmpty()) {
                    Text(text = step.title!!, style = AppFonts.size16Body1, fontWeight = FontWeight.Bold, color = AppColors.main)
                }
                Text(text = step.content ?: "", style = AppFonts.size14Body2, color = AppColors.text)
            }
        }
    }
}

@Composable
fun AiRecipeBottomBar(
    uiState: AiRecipeUiState,
    isFromHistory: Boolean,
    ingredients: List<Food>,
    viewModel: AiRecipeDetailViewModel,
    onRegenerateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        // 저장하기 버튼
        Button(
            onClick = { viewModel.toggleSaveState() },
            enabled = !uiState.isSaving,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isSaved) AppColors.point else AppColors.light5Gray,
                contentColor = if (uiState.isSaved) AppColors.white else AppColors.light3Gray
            )
        ) {
            if (uiState.isSaving) {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AppColors.white, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "레시피 저장하기", // ✅ 텍스트 상태 변경 적용
                        style = AppFonts.size16Body1,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Image(
                        painter = painterResource(id = if (uiState.isSaved) R.drawable.filled_heart else R.drawable.empty_heart),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 다시 생성하기 버튼
        if (!isFromHistory) {
            Button(
                onClick = onRegenerateClick,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.main, contentColor = AppColors.white)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "다시 생성하기", style = AppFonts.size16Body1, modifier = Modifier.padding(end = 8.dp))
                    Image(painter = painterResource(id = R.drawable.refresh_icon), contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.white,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "레시피 다시 생성",
                    style = AppFonts.size19Title3,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.text
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "현재 레시피 대신 새로운 레시피를\n다시 생성하시겠습니까?",
                    style = AppFonts.size14Body2,
                    color = AppColors.light3Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.light5Gray,
                            contentColor = AppColors.text
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("취소", style = AppFonts.size16Body1)
                    }
                    // 확인 버튼
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.main,
                            contentColor = AppColors.white
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("확인", style = AppFonts.size16Body1)
                    }
                }
            }
        }
    }
}
