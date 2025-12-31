package com.foodkeeper.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ 누적 인디케이터: 현재 페이지를 포함하여 이전 페이지들까지 AppColors.point로 채움
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { iteration ->
                    // 현재 인덱스가 현재 페이지보다 작거나 같으면 색을 채움
                    val isFilled = iteration <= pagerState.currentPage
                    val color = if (isFilled) AppColors.point else Color.LightGray
                    Surface(
                        modifier = Modifier.size(13.dp),
                        shape = CircleShape,
                        color = color
                    ) {}
                }
            }


            // ✅ 버튼 컨테이너: 건너뛰기가 사라져도 전체 높이가 유지되도록 함
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp), // 건너뛰기(56dp) + 간격(18dp) + 다음버튼(56dp) 정도의 고정 높이
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 건너뛰기 버튼: 마지막 페이지가 아닐 때만 노출
                    if (pagerState.currentPage != pages.size - 1) {
                        TextButton(
                            onClick = {

                                scope.launch {
                                    pagerState.animateScrollToPage(pages.size - 1)
                                }

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(23.dp),
                        ) {
                            Text(
                                text = "건너뛰기",
                                style = AppFonts.size19Title3,
                                color = AppColors.text
                            )
                        }
                    } else {
                        // 마지막 페이지일 때는 건너뛰기 버튼 자리를 빈 공간으로 채워 위치 고정
                        Spacer(modifier = Modifier.height(56.dp))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 다음 / 시작하기 버튼
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                viewModel.completeOnboarding { onNavigateToLogin() }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.main,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "지금 바로 시작하기" else "다음으로",
                            style = AppFonts.size19Title3
                        )
                    }
                }
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
        // ✅ 요구사항: 화면 맨 위에서 118dp 떨어짐
        Spacer(modifier = Modifier.height(118.dp))

        Text(
            text = onboardingPage.title,
            color= AppColors.main,
            style =  AppFonts.size26Title1,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(60.dp))

        // TODO: 실제 프로젝트의 아이콘이나 이미지를 넣으세요.
        // ✅ png 이미지 적용
        Image(
            painter = painterResource(id = onboardingPage.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(283.dp) // 이미지 크기 조절
                .padding(bottom = 20.dp)
        )

    }
}

sealed class OnboardingPage(
    val title: String,
    val imageRes: Int // 실제로는 Painter나 ImageVector를 쓰는 것이 좋습니다.
) {
    object First : OnboardingPage("냉장고 속 식료품\n등록하고 한눈에 관리해요",  imageRes = R.drawable.onboarding_1)
    object Second : OnboardingPage("유통기한 임박하면\n 알림으로 미리 체크해요",   imageRes = R.drawable.onboarding_2)
    object Third : OnboardingPage("지금 있는 재료로\n AI 레시피 추천까지!",   imageRes = R.drawable.onboarding_3)
}
