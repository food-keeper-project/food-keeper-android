package com.foodkeeper.feature.airecipe

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.foodkeeper.core.ui.util.AppColors

@Composable
fun SaveFloatingButton(uiState: AiRecipeUiState, viewModel: AiRecipeDetailViewModel, modifier: Modifier) {
    Button(
        onClick = { viewModel.toggleSaveState() },
        enabled = !uiState.isSaving,
        modifier = modifier
            .height(44.dp), // 1. 높이를 약간 줄이고 padding 조절
        shape = RoundedCornerShape(22.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp), // 2. 버튼 내부 패딩 사용
        colors = ButtonDefaults.buttonColors(
            containerColor = if (uiState.isSaved) AppColors.main else AppColors.light5Gray,
        )
    ) {
        if (uiState.isSaving) {
            // 3. 인디케이터 크기 조절 (버튼 높이에 맞게)
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (uiState.isSaved) AppColors.white else AppColors.main
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.isSaved) "레시피 저장됨" else "레시피 저장하기",
                    color = if (uiState.isSaved) AppColors.white else AppColors.light3Gray,
                    style = com.foodkeeper.core.ui.util.AppFonts.size14Body2 // 4. 폰트 명시 (너무 크면 잘림)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    painter = painterResource(
                        id = if (uiState.isSaved) R.drawable.filled_heart
                        else R.drawable.empty_heart
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (uiState.isSaved) AppColors.white else AppColors.light3Gray
                )
            }
        }
    }
}

@Composable
fun RegenerateFloatingButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.white)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "다시 생성하기",
                style = com.foodkeeper.core.ui.util.AppFonts.size14Body2,
                color = AppColors.main
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.refresh_icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = AppColors.main
            )
        }
    }
}

