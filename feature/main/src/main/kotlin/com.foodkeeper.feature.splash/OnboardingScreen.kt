package com.foodkeeper.feature.splash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage.First,
        OnboardingPage.Second,
        OnboardingPage.Third
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. 페이저 (이미지와 텍스트)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { position ->
            PagerScreen(onboardingPage = pages[position])
        }

        // 2. 하단 인디케이터 및 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 점(Dot) 인디케이터
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = color
                    ) {}
                }
            }

            // 버튼 로직
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        // 마지막 페이지에서 클릭 시 상태 저장 후 이동
                        viewModel.completeOnboarding { onNavigateToLogin() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(text = if (pagerState.currentPage == pages.size - 1) "시작하기" else "다음")
            }
        }
    }
}

@Composable
fun PagerScreen(onboardingPage: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TODO: 실제 프로젝트의 아이콘이나 이미지를 넣으세요.
        Text(
            text = onboardingPage.icon,
            fontSize = 100.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )
        Text(
            text = onboardingPage.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = onboardingPage.description,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 40.dp, end = 40.dp)
        )
    }
}

sealed class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String // 실제로는 Painter나 ImageVector를 쓰는 것이 좋습니다.
) {
    object First : OnboardingPage("식재료 관리", "유통기한을 놓치지 않게\n푸드키퍼가 도와드려요.", "🍎")
    object Second : OnboardingPage("알림 설정", "유통기한이 임박하면\n똑똑하게 알려드릴게요.", "🔔")
    object Third : OnboardingPage("레시피 추천", "냉장고 속 재료로 만들 수 있는\n최고의 레시피를 확인하세요.", "🍳")
}
