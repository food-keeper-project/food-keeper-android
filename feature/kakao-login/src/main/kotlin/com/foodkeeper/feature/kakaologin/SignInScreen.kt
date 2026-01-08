package com.foodkeeper.feature.kakaologin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar 사용을 위해 추가
@Composable
fun SignInScreen(
    onNavigateToMain: () -> Unit, // 메인 홈으로 이동
    onBackToLoginSelect: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // ✅ 뒤로가기 동작을 처리하는 공통 함수
    val handleBackPress = {
        when (uiState.currentStep) {
            // ✅ SignInStep -> AuthStep으로 변경
            AuthStep.PW_INPUT -> viewModel.setStep(AuthStep.ID_INPUT)
            AuthStep.FIND_ACCOUNT -> viewModel.setStep(AuthStep.ID_INPUT)
            else -> onBackToLoginSelect()
        }
    }

    // 물리적 뒤로가기 버튼 처리
    BackHandler {
        handleBackPress()
    }

    // ✅ Scaffold를 사용하여 전체 레이아웃 구성
    Scaffold(
        containerColor = AppColors.white, // 배경색 지정
        topBar = {
            // ✅ 요청하신 TopAppBar 추가
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.white
                ),
                title = {
                    Text(
                        text = when (uiState.currentStep) {
                            AuthStep.FIND_ACCOUNT -> "계정 찾기"
                            else -> "로그인"
                        },
                        style = AppFonts.size22Title2,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        handleBackPress() // 공통 뒤로가기 함수 호출
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues -> // Scaffold가 제공하는 패딩 적용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 상단바 아래부터 UI가 시작되도록 패딩 적용
                .padding(24.dp), // 기존의 화면 내부 패딩
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ 애니메이션 효과 추가 (슬라이드 전환)
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "SignInStepTransition"
            ) { step ->
                when (step) {
                    // ✅ SignInStep -> AuthStep으로 변경
                    AuthStep.ID_INPUT -> {
                        IdInputColumn(
                            value = uiState.userId,
                            onValueChange = viewModel::updateId,
                            onNext = {
                                focusManager.clearFocus()
                                viewModel.setStep(AuthStep.PW_INPUT)
                            },
                            onFindAccount = { viewModel.setStep(AuthStep.FIND_ACCOUNT) }
                        )
                    }
                    AuthStep.PW_INPUT -> {
                        PwInputColumn(
                            value = uiState.userPw,
                            onValueChange = viewModel::updatePw,
                            isLoading = uiState.isLoading,
                            onLogin = {
                                focusManager.clearFocus()
                                viewModel.signIn()
                            },
                            onFindAccount = { viewModel.setStep(AuthStep.FIND_ACCOUNT) }
                        )
                    }
                    // ✅ FindAccountColumn 호출부 수정
                    AuthStep.FIND_ACCOUNT -> {
                        FindAccountColumn(
                            findState = uiState.findAccountState,
                            onTabSelected = viewModel::onTabSelected,
                            onEmailChange = viewModel::onFindAccountEmailChange,
                            onCodeChange = viewModel::onFindAccountCodeChange,
                            onSendCodeClick = viewModel::sendFindAccountCode,
                            onVerifyCodeClick = viewModel::verifyFindAccountCode,
                            onNewPasswordChange = viewModel::onNewPasswordChange,
                            onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChange,
                            onResetPasswordClick = viewModel::resetPassword
                        )
                    }
                }
            }
        }
    }

    // 로그인 성공/실패 관련 로직은 UI와 분리되어 있으므로 그대로 둡니다.
    LaunchedEffect(uiState.isSignInSuccess) {
        if (uiState.isSignInSuccess) {
            onNavigateToMain()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }
}


@Composable
fun IdInputColumn(
    value: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
    onFindAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("아이디", style = AppFonts.size16Body1)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .background(AppColors.main),
            placeholder = { Text("아이디 입력") },
            singleLine = true
        )
        ForgotAccountLink(onClick = onFindAccount)

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.main)
                .height(56.dp),
            enabled = value.isNotBlank()
        ) { Text("다음으로") }
    }
}

@Composable
fun PwInputColumn(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onFindAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("비밀번호", style = AppFonts.size16Body1)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .background(AppColors.main),
            placeholder = { Text("비밀번호 입력") },
            visualTransformation = PasswordVisualTransformation(), // ✅ 비밀번호 숨김 처리
            singleLine = true
        )
        ForgotAccountLink(onClick = onFindAccount)

        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = value.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                        .background(AppColors.main),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인하기")
            }
        }
    }
}

@Composable
fun ForgotAccountLink(onClick: () -> Unit) {
    Text(
        text = "아이디/비밀번호가 기억이 안난다면?",
        style = AppFonts.size12Caption1.copy(textDecoration = TextDecoration.Underline),
        color = Color.Gray,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    )
}

@Composable
fun FindAccountColumn(
    findState: FindAccountUiState, // ViewModel의 상세 상태를 전달받음
    onTabSelected: (FindAccountTab) -> Unit,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onVerifyCodeClick: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ✅ 탭 구현 (TabRow)
        TabRow(
            selectedTabIndex = findState.selectedTab.ordinal,
            containerColor = AppColors.white,
            contentColor = AppColors.main,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[findState.selectedTab.ordinal]),
                    color = AppColors.main
                )
            }
        ) {
            Tab(
                selected = findState.selectedTab == FindAccountTab.ID,
                onClick = { onTabSelected(FindAccountTab.ID) },
                text = { Text("아이디 찾기", style = AppFonts.size16Body1) }
            )
            Tab(
                selected = findState.selectedTab == FindAccountTab.PASSWORD,
                onClick = { onTabSelected(FindAccountTab.PASSWORD) },
                text = { Text("비밀번호 찾기", style = AppFonts.size16Body1) }
            )
        }

        // --- 공통 UI: 이메일 입력 및 인증 ---
        if (!findState.isCodeVerified) { // 인증 완료 전까지 보임
            Text("가입 시 입력한 이메일을 입력해주세요.", style = AppFonts.size14Body2)
            OutlinedTextField(
                value = findState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth()
                    .background(AppColors.main),
                placeholder = { Text("이메일 주소 입력") }
            )
            if (findState.email.isNotBlank()){
                Button(
                    onClick = onSendCodeClick,
                    modifier = Modifier.fillMaxWidth().background(AppColors.main).height(56.dp)
                ) {
                    Text(if (findState.isCodeSent) "인증번호 재발송" else "인증번호 발송")
                }
            }

            if (findState.isCodeSent) {
                OutlinedTextField(
                    value = findState.code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth()
                        .background(AppColors.main),
                    placeholder = { Text("인증번호 입력") }
                )
                if (findState.code.isNotBlank()) {
                    Button(
                        onClick = onVerifyCodeClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp).background(AppColors.main)
                    ) {
                        Text("인증번호 확인")
                    }
                }
            }
        } else { // 인증 완료 후
            // ✅ "아이디 찾기" 탭의 결과
            if (findState.selectedTab == FindAccountTab.ID) {
                // TODO: 서버에서 찾은 아이디를 여기에 표시
                Text("회원님의 아이디는 example 입니다.", style = AppFonts.size16Body1)
            }
        }

        // --- 비밀번호 찾기 전용 UI ---
        if (findState.isCodeVerified && findState.selectedTab == FindAccountTab.PASSWORD) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("새로운 비밀번호를 입력해주세요.", style = AppFonts.size14Body2)
                OutlinedTextField(
                    value = findState.newPassword,
                    onValueChange = onNewPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("새 비밀번호 (영문, 숫자 포함 8~20자)") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = findState.newPasswordConfirm,
                    onValueChange = onNewPasswordConfirmChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("새 비밀번호 확인") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Button(
                    onClick = onResetPasswordClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    // TODO: 비밀번호 유효성 검사 로직 추가
                    enabled = findState.newPassword.isNotBlank() && findState.newPassword == findState.newPasswordConfirm
                ) {
                    Text("비밀번호 재설정")
                }
            }
        }
    }
}
