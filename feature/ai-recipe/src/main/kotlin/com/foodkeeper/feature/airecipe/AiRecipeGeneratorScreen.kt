package com.foodkeeper.feature.airecipe

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
fun AiRecipeGeneratorScreen(
    ingredients: List<Food>,
    onBackClick: () -> Unit,
    viewModel: AiRecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        viewModel.generateRecipe(ingredients)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAllState() // ViewModel에 public으로 열어준 뒤 호출
        }
    }
    Scaffold(
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
        containerColor = AppColors.white
    ){ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 2. 에러가 발생했을 때 (추가됨)
                uiState.isError -> {
                    AiErrorScreen(
                        onRetry = { viewModel.generateRecipe(ingredients) }
                    )
                }
                // 1. 로딩 중일 때
                uiState.isLoading|| uiState.title.isEmpty() -> {
                    AiLoadingScreen()
                }



                // 3. 정상적으로 레시피가 생성되었을 때
                else -> {
                    RecipeDetailContent(uiState, viewModel)

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SaveFloatingButton(uiState, viewModel, modifier = Modifier.weight(1f))
                        RegenerateFloatingButton(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        RecipeConfirmDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = { viewModel.generateRecipe(ingredients) }
        )
    }
}

@Composable
fun AiErrorScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 실패를 상징하는 이미지 (기존 로딩 이미지와 다른 것이 있다면 교체 가능)
            Image(
                painter = painterResource(id = R.drawable.ai_loading), // 에러용 이미지가 있다면 변경하세요
                contentDescription = "에러 이미지",
                modifier = Modifier
                    .height(200.dp)
                    .width(170.dp)
                    .alpha(0.5f) // 약간 흐리게 처리
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "레시피 생성 실패!",
                style = AppFonts.size19Title3,
                fontWeight = FontWeight.Bold,
                color = AppColors.black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "네트워크 상태를 확인하거나\n다시 시도해주세요.",
                style = AppFonts.size14Body2,
                color = AppColors.light3Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 다시 시도 버튼
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.main,
                    contentColor = AppColors.white
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(
                    text = "다시 시도하기",
                    style = AppFonts.size16Body1,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun AiLoadingScreen() {
    // ✅ 점(...) 애니메이션을 위한 상태 설정 (0부터 3까지 반복)
    val infiniteTransition = rememberInfiniteTransition(label = "loadingDots")
    val dotCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 4, // 0, 1, 2, 3까지 표현
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotCount"
    )

    // 숫자에 따라 점의 개수 결정
    val dots = ".".repeat(dotCount)

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
            Image(
                painter = painterResource(id = R.drawable.ai_loading),
                contentDescription = "로딩 이미지",
                modifier = Modifier
                    .height(280.dp)
                    .width(170.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ 텍스트 뒤에 가변적인 dots 변수를 붙여줍니다.
            Text(
                text = "AI가 레시피를 생성중입니다$dots",
                style = AppFonts.size16Body1,
                color = AppColors.main,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth() // 텍스트 길이가 변해도 중앙 정렬 유지
            )
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
                    color = AppColors.black
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
                            contentColor = AppColors.black
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