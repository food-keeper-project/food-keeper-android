import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

import com.foodkeeper.feature.airecipe.AiRecipeDetailViewModel
import com.foodkeeper.feature.airecipe.RecipeDetailContent
import com.foodkeeper.feature.airecipe.SaveFloatingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecipeHistoryDetailScreen(
    recipeId: Long,
    onBackClick: () -> Unit,
    viewModel: AiRecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// enabled = true는 뒤로가기 버튼을 항상 가로채겠다는 의미입니다.
    BackHandler(enabled = true) {
        // 시스템 뒤로가기 버튼을 누르면 onBackClick 함수를 실행합니다.
        onBackClick()
    }
    LaunchedEffect(recipeId) {
        viewModel.fetchRecipeDetail(recipeId)
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
        }

        ,

        containerColor = AppColors.white
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                // 1. 에러 발생 시 에러 화면 표시
                uiState.isError -> {
                    // 앞서 만든 공통 에러 화면(HomeErrorScreen 등)이나
                    // 레시피 전용 에러 화면을 호출하세요.
                    AiHistoryErrorScreen(
                        onRetry = { viewModel.fetchRecipeDetail(recipeId) }
                    )
                }

                // 2. 로딩 중이거나, 로딩은 끝났지만 데이터(제목)가 아직 없는 경우 (깜빡임 방지)
                uiState.isLoading || uiState.title.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.main)
                    }
                }

                // 3. 데이터가 확실히 로드된 경우에만 UI 노출
                else -> {
                    RecipeDetailContent(uiState, viewModel)

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 생성 스크린과 동일한 비율을 맞추기 위해 0.5f 사용
                        Spacer(modifier = Modifier.weight(0.5f))

                        SaveFloatingButton(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }

        }
    }
}

@Composable
fun AiHistoryErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.white)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "레시피를 불러오지 못했습니다",
            style = AppFonts.size19Title3,
            fontWeight = FontWeight.Bold,
            color = AppColors.black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "서버 오류가 발생하였습니다.\n잠시 후 다시 시도해주세요.",
            style = AppFonts.size14Body2,
            color = AppColors.light2Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 다시 시도 버튼
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.main,
                contentColor = AppColors.white
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "다시 시도하기",
                style = AppFonts.size16Body1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}