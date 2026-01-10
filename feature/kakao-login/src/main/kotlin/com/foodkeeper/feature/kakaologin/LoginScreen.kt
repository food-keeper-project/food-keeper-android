package com.foodkeeper.feature.kakaologin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.AppString


@Composable
fun LoginScreen(
    // 1. hiltViewModel()을 사용하여 뷰모델 주입
    viewModel: LoginViewModel = hiltViewModel(),
    // 2. 외부(app 모듈)에서 넘겨줄 성공 콜백
    onLoginSuccess: () -> Unit
) {
    // ViewModel의 uiState를 구독하여 상태가 변경될 때마다 리컴포지션을 트리거합니다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white), // Box에 배경색을 설정하여 전체 화면을 덮도록 함
        contentAlignment = Alignment.Center
    ) {
        if (uiState is LoginUiState.Loading) {
            // 로딩 상태일 때 화면 중앙에 원형 프로그레스 바를 표시합니다.
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp), // 좌우 패딩만 설정
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ 상단 공간을 차지하는 Spacer
                Spacer(modifier = Modifier.weight(1f))

                // --- 중앙 컨텐츠 ---
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = AppString.appName,
                    style = AppFonts.size50Title0,
                    color = AppColors.main
                )

                // ✅ 중앙과 하단 버튼 사이의 공간을 모두 차지하는 Spacer
                Spacer(modifier = Modifier.weight(1f))

                // --- 하단 컨텐츠 ---
                // 3. 카카오로 시작하기 버튼
                Button(
                    onClick = { viewModel.login(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp), // 모서리를 약간 둥글게 조절
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE500), // 카카오 공식 노란색
                        contentColor = Color(0xFF191919)    // 카카오 공식 텍스트색
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 카카오 로고 아이콘이 있다면 여기에 추가
                        // Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "카카오로 시작하기",
                            style = AppFonts.size19Title3,
                            color= AppColors.black
                        )
                    }
                }
                // ✅ 화면 하단 여백
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // UI 상태가 변경될 때마다 특정 액션(Side Effect)을 수행합니다.
    // LaunchedEffect는 Composable의 생명주기 내에서 코루틴을 안전하게 실행하는 역할을 합니다.
    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Error -> {
                // 로그인 실패 시 Toast 메시지 표시
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                Log.d("TAG", "로그인 실패: ${state.message}")
            }
            is LoginUiState.Success -> {
                // 로그인 성공 시 Toast 메시지 표시 및 다음 화면으로 이동 준비
                onLoginSuccess()
            }
            else -> { /* Idle, Loading 상태에서는 별도의 Side Effect가 필요 없습니다. */ }
        }
    }
}
