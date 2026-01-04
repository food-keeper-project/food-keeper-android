import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

import com.foodkeeper.feature.profile.WithdrawalViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun WithdrawalRoute(
    onWithdrawalSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recipeCount by viewModel.recipeCount.collectAsStateWithLifecycle()
    val foodCount by viewModel.foodCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle() // ✅ 로딩 상태 추가

    LaunchedEffect(Unit) {
        viewModel.fetchAllCounts()
    }

    // ✅ 회원탈퇴 결과/에러 처리
    LaunchedEffect(Unit) {
        // 성공 처리
        viewModel.withdrawalSuccess.collectLatest { success ->
            if (success) {
                Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                onWithdrawalSuccess()
            }
        }
    }

    // ✅ 에러 메시지 처리 추가
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collectLatest { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }


    WithdrawalScreen(
        foodCount = foodCount,
        recipeCount = recipeCount,
        onBackClick = onBackClick,
        isLoading = isLoading,
        onConfirmClick = { viewModel.withdraw() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    recipeCount: Long,
    foodCount: Long,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    var isAgreed by remember { mutableStateOf(false) }
// ✅ 1. 스크롤 상태와 코루틴 스코프 정의
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.white
                ),
                title = {
                    Text(
                        text = "회원탈퇴",
                        style = AppFonts.size22Title2,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        containerColor = AppColors.white
    ) { padding ->
        // ✅ 전체 레이아웃을 LazyColumn으로 변경하여 스크롤 대응
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Text(
                    text = "정말 탈퇴하시겠어요?",
                    style = AppFonts.size22Title2,
                    color = AppColors.black,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            item {
                Text(
                    text = "탈퇴 시 저장한 식재료, 레시피, 알림 정보는 모두 삭제돼요.",
                    style = AppFonts.size12Caption1,
                    color = AppColors.main,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 데이터 카드 섹션
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WithdrawalDataCard(label = "등록된 식재료", count = foodCount)
                    WithdrawalDataCard(label = "저장한 레시피", count = recipeCount)
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) } // 간격 조정

            // 유의사항 블록
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.light5Gray,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WithdrawalNoticeItem("재가입 시 이전에 저장된 식재료, 레시피 등 이용 내역은 복원되지 않습니다.")
                        WithdrawalNoticeItem("탈퇴 후 개인정보는 관련 법령에 따라 일정 기간 안전하게 보관되며, 이후 자동 파기됩니다.")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }

            // 동의 체크박스
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isAgreed = !isAgreed
                        // ✅ 3. 체크 시 맨 아래로 스크롤 이동 로직 추가
                        if (isAgreed) {
                            scope.launch {
                                // 마지막 아이템 인덱스(현재 LazyColumn은 약 8~9개 아이템으로 구성됨)
                                // 여유 있게 100 등 큰 숫자를 넣어도 상관없습니다.
                                scrollState.animateScrollToItem(index = 99)
                            }
                        }
                    }
                ) {
                    Checkbox(
                        enabled = !isLoading,
                        checked = isAgreed,
                        onCheckedChange = { checked ->
                            isAgreed = checked
                            // ✅ 4. 체크박스 직접 클릭 시에도 이동
                            if (checked) {
                                scope.launch {
                                    scrollState.animateScrollToItem(index = 99)
                                }
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = AppColors.main)
                    )
                    Text("위 유의사항을 모두 숙지했고 탈퇴에 동의합니다.")
                }
            }

            // 하단 버튼 섹션
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = !isLoading,
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAgreed) AppColors.light4Gray else AppColors.main,
                            contentColor = Color.White
                        )
                    ) {
                        Text("더 써볼래요", style = AppFonts.size16Body1)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onConfirmClick,
                        enabled = isAgreed&& !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAgreed) AppColors.main else AppColors.light4Gray,
                            disabledContainerColor = AppColors.light4Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("탈퇴하기", style = AppFonts.size16Body1)
                    }
                }
            }
        }
        // ✅ 3. 로딩 인디케이터 표시 (화면 중앙)
        if (isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.1f) // 반투명 배경
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = AppColors.main)
                }
            }
        }
    }
}

@Composable
fun WithdrawalNoticeItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // 점과 글자 사이 간격
    ) {
        Text(
            text = "•", // 리스트 점
            style = AppFonts.size10Caption2,
            color = Color.Black
        )
        Text(
            text = text,
            style = AppFonts.size10Caption2,
            color = AppColors.black,
            lineHeight = 20.sp // 줄간격 추가로 가독성 향상
        )
    }
}
@Composable
fun WithdrawalDataCard(label: String, count: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.main),
        color = AppColors.white
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 레이블 (등록된 식재료)
            Text(
                text = label,
                style = AppFonts.size16Body1,
                color = AppColors.black
            )

            // 2. 개수 (레이블 바로 옆에 배치)
            Spacer(modifier = Modifier.width(8.dp)) // 레이블과 숫자 사이 간격
            Text(
                text = "${count}개",
                style = AppFonts.size16Body1,
                color = AppColors.main,
                fontWeight = FontWeight.ExtraBold
            )

            // 3. 중간 여백 (남은 공간을 다 채워서 '즉시 소멸'을 오른쪽 끝으로 밀어냄)
            Spacer(modifier = Modifier.weight(1f))

            // 4. 즉시 소멸 (count가 0보다 클 때만 오른쪽 끝에 표시)
            if (count > 0) {
                Text(
                    text = "즉시 소멸",
                    style = AppFonts.size12Caption1,
                    color = AppColors.main,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


