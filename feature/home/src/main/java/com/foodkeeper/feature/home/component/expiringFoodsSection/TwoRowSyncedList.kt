package com.foodkeeper.feature.home.component.expiringFoodsSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.foodkeeper.core.domain.model.Food

/**
 * 2줄로 홀짝 배치되는 리스트
 * 첫째 줄: 0, 2, 4, 6...
 * 둘째 줄: 1, 3, 5, 7...
 */
@Composable
fun TwoRowSyncedList(
    foodItems: List<Food>,
    onFoodItemClick: (Food) -> Unit
) {
    val firstRowItems = foodItems.filterIndexed { index, _ -> index % 2 == 0 }
    val secondRowItems = foodItems.filterIndexed { index, _ -> index % 2 == 1 }

    val firstLazyListState = rememberLazyListState()
    val secondLazyListState = rememberLazyListState()

    // ✨ 스크롤 동기화 로직
    LaunchedEffect(firstLazyListState.isScrollInProgress) {
        if (firstLazyListState.isScrollInProgress) {
            snapshotFlow { firstLazyListState.firstVisibleItemIndex to firstLazyListState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    secondLazyListState.scrollToItem(index, offset)
                }
        }
    }

    LaunchedEffect(secondLazyListState.isScrollInProgress) {
        if (secondLazyListState.isScrollInProgress) {
            snapshotFlow { secondLazyListState.firstVisibleItemIndex to secondLazyListState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    firstLazyListState.scrollToItem(index, offset)
                }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 첫째 줄
        LazyRow(
            state = firstLazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(firstRowItems) { item ->
                ExpiringFoodCardCompact(
                    item = item,
                    onFoodItemClick = onFoodItemClick
                )
            }
        }

        // 둘째 줄
        LazyRow(
            state = secondLazyListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(secondRowItems) { item ->
                ExpiringFoodCardCompact(
                    item = item,
                    onFoodItemClick = onFoodItemClick
                )
            }
        }
    }
}