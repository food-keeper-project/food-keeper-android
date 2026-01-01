package com.foodkeeper.feature.home.component.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodkeeper.core.R
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.feature.home.HomeViewModel
import kotlin.collections.filter

/**
 * 레시피 추천 다이얼로그
 * 유통기한 임박 식재료를 선택하여 레시피 생성
 */
@Composable
fun RecipeRecommendationDialog(
    expiringFoods: List<Food>,
    onDismiss: () -> Unit,
    onGenerateRecipe: (List<Food>) -> Unit
) {
    // 선택된 식재료 ID 목록
    var selectedFoodIds by remember { mutableStateOf(setOf<Long>()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // 커스텀 너비 사용
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(max = 500.dp), // 최대 높이 제한
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.white
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ✨ 타이틀 (정중앙)
                    Text(
                        text = "레시피 추천",
                        style = AppFonts.size19Title3,
                        color = AppColors.text,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // ✨ 닫기 버튼 (오른쪽)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = AppColors.text
                        )
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // ✨ 부제목
                Text(
                    text = "유통기한 임박 식재료 활용 레시피 추천받기 ♪",
                    style = AppFonts.size12Caption1,
                    color = AppColors.main
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✨ 식재료 리스트 (스크롤 가능)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false), // 남은 공간 차지하되 강제 확장 X
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = expiringFoods,
                        key = { it.id }
                    ) { food ->
                        RecipeFoodItem(
                            food = food,
                            isSelected = selectedFoodIds.contains(food.id),
                            onToggle = {
                                selectedFoodIds = if (selectedFoodIds.contains(food.id)) {
                                    selectedFoodIds - food.id
                                } else {
                                    selectedFoodIds + food.id
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✨ 레시피 생성 버튼
                Button(
                    onClick = {
                        val selectedFoods = expiringFoods.filter {
                            selectedFoodIds.contains(it.id)
                        }
                        if (selectedFoods.isNotEmpty()) {
                            onGenerateRecipe(selectedFoods)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(106.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.main,
                        disabledContainerColor = AppColors.light3Gray
                    ),
                    shape = RoundedCornerShape(23.dp),
                    enabled = selectedFoodIds.isNotEmpty(),
                    contentPadding = PaddingValues(0.dp)  // ✅ 패딩 제거로 정확한 중앙 배치
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),  // ✅ 버튼 전체 크기 채우기
                        contentAlignment = Alignment.Center  // ✅ 중앙 정렬
                    ) {
                        Text(
                            text = "레시피 생성",
                            style = AppFonts.size14Body2,
                            color = AppColors.white,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 레시피 식재료 아이템
 * 체크박스로 선택 가능
 */
@Composable
fun RecipeFoodItem(
    food: Food,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val imageShape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AppColors.white, // ⭐ 항상 고정
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                indication = null, // ⭐ 클릭 시 시각 효과 제거
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggle
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 이미지 + 정보
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 이미지
            Box(
                modifier = Modifier
                    .size(41.dp)
                    .border(
                        width = 1.dp,                 // 테두리 두께
                        color = AppColors.main,    // 테두리 색 (원하는 색)
                        shape = imageShape
                    )
                    .background(
                        color = AppColors.white,
                        shape = imageShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(food.imageURL)
                        .crossfade(true)
                        .error(R.drawable.foodplaceholder)
                        .build(),
                    contentDescription = food.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(imageShape)
                )
            }

            // 이름 + D-Day
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // ⭐ 간격 8dp
                verticalAlignment = Alignment.CenterVertically       // ⭐ 이미지 중앙 기준
            ) {
                Text(
                    text = food.name,
                    style = AppFonts.size16Body1,
                    color = AppColors.text,
                )

                Text(
                    text = "D+${food.expiryDate.getDDay()}",
                    style = AppFonts.size14Body2,
                    color = AppColors.main,
                )
            }
        }

        // 오른쪽: 체크박스
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = if (isSelected) AppColors.main else AppColors.light3Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) AppColors.main else AppColors.light3Gray,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = AppColors.white,
                    modifier = Modifier.size(12.dp)
                )
        }
    }
}
