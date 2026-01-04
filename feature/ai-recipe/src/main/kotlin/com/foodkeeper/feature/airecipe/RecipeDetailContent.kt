package com.foodkeeper.feature.airecipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@Composable
fun RecipeDetailContent(uiState: AiRecipeUiState, viewModel: AiRecipeDetailViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(AppColors.white),
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
                    Text(text = uiState.title, style = AppFonts.size22Title2, color = AppColors.black)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = uiState.description, style = AppFonts.size14Body2, color = AppColors.black)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = AppColors.light6Gray, // 또는 원하는 다크그레이 색상 (ex: Color(0xFF424242))
                        shape = RoundedCornerShape(4.dp) // 모서리를 살짝 둥글게
                    )

            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.history_timer),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),

                    tint = AppColors.black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "요리 예상 소요시간", color = AppColors.black, style = AppFonts.size12Caption1)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${uiState.cookMinutes}분", color = AppColors.main, style = AppFonts.size12Caption1,fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        item {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.toggleIngredients() },
                    modifier = Modifier.fillMaxWidth()
                        .background(AppColors.light6Gray),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.light5Gray, contentColor = AppColors.black)
                ) {
                    Text(if (uiState.isIngredientsExpanded) "재료 숨기기" else "필요한 재료 보기")
                }

                if (uiState.isIngredientsExpanded) {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .background(AppColors.light6Gray)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = AppColors.light5Gray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            uiState.ingredients.forEach { (name, amount) ->
                                Text("• $name: $amount", style = AppFonts.size12Caption1, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp) // 구분선 상하 여백
                    .height(1.dp) // 구분선 두께
                    .background(AppColors.light2Gray) // 요청하신 색상
            )
        }

        itemsIndexed(uiState.steps) { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // ✅ 선이 끝까지 이어지도록 높이 설정
            ) {
                // --- 왼쪽: 번호와 연결선 ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(30.dp) // 번호 영역 너비 고정
                ) {
                    // 번호 동그라미
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                AppColors.main,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = AppFonts.size16Body1,
                            color = AppColors.white,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // 연결선 (마지막 아이템이 아닐 때만 표시)
                    if (index < uiState.steps.lastIndex) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f) // ✅ 이 weight가 아래 Spacer 영역까지 선을 끌어내림
                                .background(AppColors.main.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // --- 오른쪽: 내용 (배경 적용 영역) ---
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ✅ 실제 내용 박스에만 배경색 적용
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = AppColors.light6Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(12.dp) // 박스 내부 여백
                    ) {
                        if (!step.title.isNullOrEmpty()) {
                            Text(
                                text = step.title!!,
                                style = AppFonts.size16Body1,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = step.content ?: "",
                            style = AppFonts.size12Caption1,
                            color = AppColors.black
                        )
                    }

                    // ✅ 다음 단계와의 간격 (이 공간만큼 왼쪽 선이 내려옴)
                    if (index < uiState.steps.lastIndex) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }


        // ✅ 하단 플로팅 버튼 영역만큼의 여백 추가
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
