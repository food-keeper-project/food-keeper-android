import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

import com.foodkeeper.feature.profile.WithdrawalViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WithdrawalRoute(
    onWithdrawalSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: WithdrawalViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.withdrawalSuccess.collectLatest { success ->
            if (success) {
                Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                onWithdrawalSuccess()
            }
        }
    }

    WithdrawalScreen(
        onBackClick = onBackClick,
        onConfirmClick = { viewModel.withdraw() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    var isAgreed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // ✅ 상단바 및 뒤로가기 버튼 구현
            CenterAlignedTopAppBar(
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        containerColor = AppColors.white
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "정말 탈퇴하시겠어요?",
                style = AppFonts.size22Title2,
                color = AppColors.text,
                modifier = Modifier.padding(top = 24.dp)
            )
            //Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "탈퇴 시 저장한 식재료, 레시피, 알림 정보는 모두 삭제돼요.",
                style = AppFonts.size12Caption1,
                color = AppColors.main,
                modifier = Modifier.padding(top = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // 피그마에 있던 유의사항 리스트 박스
            //WithdrawalNoticeBox()
            // 1. 등록된 정보 카드 섹션 (Border: Main, Bg: White)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WithdrawalDataCard(label = "등록된 식재료", count = 23)
                WithdrawalDataCard(label = "저장한 레시피", count = 11)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 유의사항 블록 (Bg: lightGray5/gray1)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.light5Gray, // lightGray5가 없는 경우 gray1 사용
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
            // 동의 체크박스
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isAgreed = !isAgreed }
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = AppColors.main)
                )
                Text("위 유의사항을 모두 숙지했고 탈퇴에 동의합니다.")
            }

            Row(verticalAlignment = Alignment.CenterVertically){
                // 더 써볼래요 버튼
                Button(
                    onClick = onConfirmClick,
                    enabled = isAgreed,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAgreed) AppColors.light3Gray else AppColors.main,
                        disabledContainerColor = AppColors.main, // 비활성화 시 색상
                        contentColor = AppColors.white,
                        disabledContentColor = AppColors.white.copy(alpha = 0.5f)
                    )
                ) {
                    Text("더 써볼래요", color = Color.White)
                }
                Spacer(modifier = Modifier.width(24.dp))
                // 탈퇴 버튼
                Button(
                    onClick = onConfirmClick,
                    enabled = isAgreed,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAgreed) AppColors.main else AppColors.light3Gray,
                        disabledContainerColor = AppColors.light3Gray, // 비활성화 시 색상
                        contentColor = AppColors.white,
                        disabledContentColor = AppColors.white.copy(alpha = 0.5f)
                    )
                ) {
                    Text("탈퇴하기", color = Color.White)
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
            color = AppColors.text,
            lineHeight = 20.sp // 줄간격 추가로 가독성 향상
        )
    }
}
@Composable
fun WithdrawalDataCard(label: String, count: Int) { // count 타입을 Int로 수정
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.main), // 테두리 main 컬러
        color = AppColors.white // 배경색 화이트
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = AppFonts.size16Body1,
                color = AppColors.text
            )
            Text(
                text = "${count}개",
                style = AppFonts.size16Body1,
                color = AppColors.main,
                fontWeight = FontWeight.ExtraBold

            )
            Text(
                text = "즉시 소멸",
                style = AppFonts.size16Body1,
                color = AppColors.main // "즉시 소멸" 강조 색상
            )
        }
    }
}

