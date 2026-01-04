package com.foodkeeper.feature.home.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foodkeeper.core.R
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddWithDay

/**
 * 식재료 상세보기 다이얼로그
 */
@Composable
fun FoodDetailDialog(
    food: Food,
    onDismiss: () -> Unit, // 팝업 닫기
    onConsumption: (Food) -> Unit //식재료 소비
) {
    val imageShape = RoundedCornerShape(20.dp)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false //바깥 클릭 시 닫기 X
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        start = 24.dp,
                        end = 16.dp
                    )
                ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.white
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp), // IconButton 높이와 맞춤
                ) {
                    // ✨ 제목 (정중앙)
                    Text(
                        text = "식재료 상세보기",
                        style = AppFonts.size16Body1,
                        color = AppColors.black,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // ✨ 닫기 버튼 (오른쪽 끝)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = AppColors.black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(29.dp))

                // 이미지
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(90.dp)
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

                Spacer(modifier = Modifier.height(24.dp))

                // ✨ 식재료명
                DetailRow(
                    label = "식재료명",
                    value = food.name,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✨ 카테고리
                DetailRow(
                    label = "카테고리",
                    value = food.category,
                    valueBackground = AppColors.point
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✨ 보관방식
                DetailRow(
                    label = "보관방식",
                    value = food.storageMethod.displayName,
                    valueBackground = AppColors.point
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✨ 유통기한
                DetailRow(
                    label = "유통기한",
                    value = food.expiryDate.toyyMMddWithDay(),
                    showBadge = true,
                    dDay = food.expiryDate.getDDay()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ✨ 메모
//                if (food.memo.isNotEmpty()) {
                DetailRow(
                    label = "메모",
                    value = food.memo,
                    showBadge = false,
                    dDay = food.expiryDate.getDDay()
                )
//                }
                Spacer(modifier = Modifier.height(20.dp))
                // ✨ 소비 완료 버튼
                Button(
                    onClick = { onConsumption(food) },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(105.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.main
                    ),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text(
                        text = "소비 완료",
                        style = AppFonts.size14Body2,
                        color = AppColors.white
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


/**
 * 상세 정보 행
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = AppColors.black,
    valueBackground: Color? = null,
    showBadge: Boolean = false,
    dDay: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically // ✅ 세로 중앙 정렬
    ) {
        // 라벨 (고정 폭)
        Text(
            text = label,
            style = AppFonts.size12Caption1,
            color = AppColors.main,
            modifier = Modifier
                .width(58.dp)
                .alignByBaseline() // ✅ 첫 줄 기준선 정렬
        )

        // value 영역
        Row(
            modifier = Modifier
                .alignByBaseline(), // ✅ 첫 줄 기준선 정렬
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (valueBackground != null) {
                Surface(
                    color = valueBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = value,
                        style = AppFonts.size12Caption1,
                        color = valueColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = AppFonts.size14Body2,
                    color = valueColor,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // D-Day 뱃지
            if (showBadge) {
                Surface(
                    color = if (dDay >= 0) AppColors.main else AppColors.dartGray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (dDay >= 0) "D-$dDay" else "D+${kotlin.math.abs(dDay)}",
                        style = AppFonts.size12Caption1,
                        color = AppColors.white,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}