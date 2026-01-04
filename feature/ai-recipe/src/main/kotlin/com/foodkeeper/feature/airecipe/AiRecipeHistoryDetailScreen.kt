import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors

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
            if (uiState.isLoading) {
                // 히스토리 로딩은 스피너로 표시
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.main)
                }
            } else {
                RecipeDetailContent(uiState, viewModel)

                // ✅ 생성 스크린의 버튼 크기와 동일하게 맞추기 위해 Row 사용
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 왼쪽 빈 공간 (버튼을 중앙으로 밀어줌)
                    Spacer(modifier = Modifier.weight(0.5f))

                    // 왼쪽 절반만 저장 버튼이 차지하게 함 (생성 스크린과 동일한 weight 값 부여)
                    SaveFloatingButton(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )

                    // 오른쪽 절반은 비워둠으로써 버튼 크기를 고정 (생성 스크린의 '다시 생성' 버튼 자리)
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }
    }
}