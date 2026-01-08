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
import androidx.compose.ui.text.style.TextDecoration
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
    onLoginSuccess: () -> Unit,
    // ✅ 자체 로그인 및 회원가입 화면 이동을 위한 콜백 추가 가능
    onIdLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    // ViewModel의 uiState를 구독하여 상태가 변경될 때마다 리컴포지션을 트리거합니다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.white),
        contentAlignment = Alignment.Center
    ) {
        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator()
        } else {
            // ✅ 수직 배치를 SpaceBetween으로 설정하여 상단과 하단을 양 끝으로 밀어냄
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 20.dp), // 상하 최소 여백
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // [1. 상단 로고 영역] - weight(1f)를 주어 남는 공간을 다 차지하게 함
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center // ✅ 상단 영역의 정중앙에 배치
                ) {
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
                }

                // [2. 하단 버튼 및 링크 영역]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 카카오로 시작하기
                    Button(
                        onClick = { viewModel.login(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE500),
                            contentColor = Color(0xFF191919)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "카카오로 시작하기",
                            style = AppFonts.size16Body1,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    // 아이디로 시작하기
                    Button(
                        onClick = onIdLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.light4Gray,
                            contentColor = AppColors.black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "아이디로 시작하기",
                            style = AppFonts.size16Body1,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 회원가입 링크
                    Text(
                        text = "회원가입하여 시작하기",
                        style = AppFonts.size14Body2.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        color = AppColors.light2Gray,
                        modifier = Modifier
                            .clickable { onSignUpClick() }
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp)) // 최하단 바닥 여백
                }
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
                //Toast.makeText(context, "로그인 성공! 토큰: ${state.token}", Toast.LENGTH_SHORT).show()
                // TODO: 여기서 내비게이션 라이브러리를 사용해 홈 화면 등으로 이동하는 코드를 작성합니다.
                // 예: navigator.navigateToHome()
                // 3. 성공 시점에 콜백 호출!
                onLoginSuccess()
            }
            else -> { /* Idle, Loading 상태에서는 별도의 Side Effect가 필요 없습니다. */ }
        }
    }

//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        if (uiState is LoginUiState.Loading) {
//            // 로딩 상태일 때 화면 중앙에 원형 프로그레스 바를 표시합니다.
//            CircularProgressIndicator()
//        } else {
//            // 로딩 상태가 아닐 때 카카오 로그인 버튼을 표시합니다.
//            KakaoLoginButton(
//                onClick = {
//                    // 버튼 클릭 시 ViewModel의 login 함수를 호출합니다.
//                    viewModel.login()
//                }
//            )
//        }
//    }
}

@Composable
private fun KakaoLoginButton(onClick: () -> Unit) {
    // 실제 카카오 로그인 버튼 이미지는 res/drawable 폴더에 추가해야 합니다.
    // 카카오 디자인 가이드에 맞는 공식 이미지를 사용하는 것을 권장합니다.
    // 예: R.drawable.kakao_login_large_wide
    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .clickable(onClick = onClick) // 클릭 이벤트를 Box에 연결
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painterResource(R.drawable.kakao_login_medium_narrow),
            // 파일 이름에 맞춰 수정
            contentDescription = "카카오로 시작하기"
        )
    }
}

//
//@Preview(showBackground = true)
//@Composable
//private fun LoginScreenPreview() {
//    // 미리보기에서는 실제 ViewModel 없이 UI 컴포넌트만 확인할 수 있습니다.
//    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        KakaoLoginButton {}
//    }
//}
