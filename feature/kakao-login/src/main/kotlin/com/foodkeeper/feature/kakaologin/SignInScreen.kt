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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.isEmailValid
import com.foodkeeper.core.ui.util.isPasswordValid

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
// ✅✅✅ 핵심 수정 2: 화면 전체를 Box로 감싸 로딩 UI를 추가합니다.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // 자식 요소를 중앙에 배치
    ) {
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
                                // --- 아이디 찾기 ---
                                onSendFindIdCodeClick = viewModel::sendFindIdCode,
                                onFindIdCodeChange = viewModel::onFindIdCodeChange,
                                onVerifyFindIdCodeClick = viewModel::verifyFindIdCode,
                                onFindIdEmailChange = viewModel::onFindIdEmailChange,
                                // --- 비밀번호 찾기 ---
                                onFindPwIdChange = viewModel::onFindPwIdChange,
                                onFindPwEmailChange = viewModel::onFindPwEmailChange,
                                onSendFindPwCodeClick = viewModel::sendFindPwCode,
                                onFindPwCodeChange = viewModel::onFindPwCodeChange,
                                onVerifyFindPwCodeClick = viewModel::verifyFindPwCode,
                                onNewPasswordChange = viewModel::onNewPasswordChange,
                                onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChange,
                                onResetPasswordClick = viewModel::resetPassword,
// ✅ 7. 최상위에서 선언한 focusManager를 전달합니다.
                                //focusManager = focusManager
                            )
                        }

                    }
                }
            }
        }

        // ✅✅✅ 핵심 수정 3: isLoading 상태일 때 보여줄 전체 화면 로딩 UI
        if (uiState.isLoading) {
            // 반투명한 배경 (Dim 처리 효과)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.light6Gray.copy(alpha = 0.5f)) //
                    .pointerInput(Unit) { detectTapGestures { } }, // 로딩 중 배경 터치 막기
                contentAlignment = Alignment.Center
            ) {
                // 로딩 애니메이션
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp), // 버튼 안보다 훨씬 크게
                    color = AppColors.main, // 어두운 배경이므로 흰색이 잘 보임
                    strokeWidth = 4.dp
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.white),
            placeholder = { Text("아이디 입력") },
            singleLine = true,
            // ✅ 색상 커스텀 추가
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
            )
        )
        ForgotAccountLink(onClick = onFindAccount)

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.white)
                .height(56.dp),
            enabled = value.isNotBlank(),
            // ✅ 버튼 색상 커스텀 추가
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.main, // 활성화 상태일 때 배경색
                contentColor = AppColors.white, // 활성화 상태일 때 글자색
                disabledContainerColor = AppColors.light3Gray, // 비활성화 상태일 때 배경색
                disabledContentColor = AppColors.light5Gray // 비활성화 상태일 때 글자색
            )
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
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.white),
            // ✅ 색상 커스텀 추가
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
            ),
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
            enabled = value.isNotBlank() && !isLoading,
            // ✅ 버튼 색상 커스텀 추가
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.main, // 활성화 상태일 때 배경색
                contentColor = AppColors.white, // 활성화 상태일 때 글자색
                disabledContainerColor = AppColors.light3Gray, // 비활성화 상태일 때 배경색
                disabledContentColor = AppColors.light5Gray // 비활성화 상태일 때 글자색
            )
        ) {
                Text("로그인하기")
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
fun FindAccountColumn( // ✅ 1. FocusManager를 파라미터로 받습니다.
    //focusManager: FocusManager,
    findState: FindAccountUiState,
    onTabSelected: (FindAccountTab) -> Unit,
    // --- 아이디 찾기 ---
    onFindIdEmailChange: (String) -> Unit,
    onSendFindIdCodeClick: () -> Unit,
    onFindIdCodeChange: (String) -> Unit,
    onVerifyFindIdCodeClick: () -> Unit,
    // --- 비밀번호 찾기 ---
    onFindPwIdChange: (String) -> Unit,
    onFindPwEmailChange: (String) -> Unit,
    onSendFindPwCodeClick: () -> Unit,
    onFindPwCodeChange: (String) -> Unit,
    onVerifyFindPwCodeClick: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // --- 탭 ---
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

        // --- 각 탭에 맞는 컨텐츠 표시 ---
        when (findState.selectedTab) {
            FindAccountTab.ID -> {
                // ✅ 아이디 찾기 UI - ViewModel 상태 구조에 맞게 속성 접근 방식 수정
                FindIdContent(
                    email = findState.findIdState.email,
                    onEmailChange = onFindIdEmailChange,
                    isCodeSent = findState.findIdState.isCodeSent,
                    onSendCodeClick = onSendFindIdCodeClick,
                    code = findState.findIdState.code,
                    onCodeChange = onFindIdCodeChange,
                    onVerifyCodeClick = onVerifyFindIdCodeClick,
                    isVerified = findState.findIdState.isVerified,
                    foundId = findState.findIdState.foundId,
                    timerSeconds = findState.findIdState.timerSeconds,
                    // ✅ 2. 받은 focusManager를 그대로 하위 함수에 전달합니다.
                    //focusManager = focusManager
                )
            }

            FindAccountTab.PASSWORD -> {
                // ✅ 비밀번호 찾기 UI - ViewModel 상태 구조에 맞게 속성 접근 방식 수정
                FindPasswordContent(
                    id = findState.findPasswordState.id,
                    onIdChange = onFindPwIdChange,
                    email = findState.findPasswordState.email,
                    onEmailChange = onFindPwEmailChange,
                    isCodeSent = findState.findPasswordState.isCodeSent,
                    onSendCodeClick = onSendFindPwCodeClick,
                    code = findState.findPasswordState.code,
                    onCodeChange = onFindPwCodeChange,
                    onVerifyCodeClick = onVerifyFindPwCodeClick,
                    isVerified = findState.findPasswordState.isVerified,
                    newPassword = findState.findPasswordState.newPassword,
                    onNewPasswordChange = onNewPasswordChange,
                    newPasswordConfirm = findState.findPasswordState.newPasswordConfirm,
                    onNewPasswordConfirmChange = onNewPasswordConfirmChange,
                    onResetPasswordClick = onResetPasswordClick,
                    timerSeconds = findState.findPasswordState.timerSeconds,
                    // ✅ 2. 받은 focusManager를 그대로 하위 함수에 전달합니다.
                    //focusManager = focusManager
                )
            }
        }
    }
}
// --- 아이디 찾기 UI ---
@Composable
private fun FindIdContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isCodeSent: Boolean,
    onSendCodeClick: () -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    isVerified: Boolean,
    foundId: String?,
    timerSeconds: Int,
    //focusManager: FocusManager
) {
    if (!isVerified) {
        // --- 인증 전 ---
        // ✅ Column에서 modifier = Modifier.pointerInput(Unit) { ... } 부분을 완전히 제거했습니다.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("가입 시 입력한 이메일을 입력해주세요.", style = AppFonts.size14Body2)
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("이메일 주소 입력") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white,
                    unfocusedContainerColor = AppColors.white,
                    focusedIndicatorColor = AppColors.point,
                    unfocusedIndicatorColor = AppColors.main
                ),
                isError = email.isNotBlank() && !email.isEmailValid(),
                supportingText = {
                    if (email.isNotBlank() && !email.isEmailValid()) {
                        Text(
                            text = "올바른 이메일 형식을 입력해주세요.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                // ✅ 키보드 액션을 focusManager.clearFocus()로 통일
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                keyboardActions = KeyboardActions(
//                    onDone = { focusManager.clearFocus() }
//                ),
                singleLine = true
            )

            // (이하 버튼 및 다른 UI 로직은 이전과 동일)
            if (email.isNotBlank()) {
                val isTimerRunning = timerSeconds > 0
                Button(
                    onClick = onSendCodeClick,
                    enabled = !isTimerRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.main,
                        contentColor = AppColors.white,
                        disabledContainerColor = AppColors.light3Gray,
                        disabledContentColor = AppColors.light5Gray
                    )
                ) {
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    val timerText = String.format("%02d:%02d", minutes, seconds)
                    Text(if (isCodeSent) (if (isTimerRunning) "인증번호 재발송 ($timerText)" else "인증번호 재발송") else "인증번호 발송")
                }
            }

            if (isCodeSent) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("인증번호 입력") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.white,
                        unfocusedContainerColor = AppColors.white,
                        focusedIndicatorColor = AppColors.point,
                        unfocusedIndicatorColor = AppColors.main
                    ),
                    // ✅✅✅ 핵심 수정: 키보드 타입을 숫자 전용으로 변경 ✅✅✅
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(
//                        onDone = { focusManager.clearFocus() }
//                    ),
                    singleLine = true
                )
                if (code.isNotBlank()) {
                    Button(
                        onClick = onVerifyCodeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.main,
                            contentColor = AppColors.white,
                            disabledContainerColor = AppColors.light3Gray,
                            disabledContentColor = AppColors.light5Gray
                        )
                    ) { Text("인증번호 확인") }
                }
            }
        }
    } else {
        // --- 인증 후 ---
        Text(
            text = if (foundId != null) "회원님의 아이디는 $foundId 입니다." else "가입된 정보를 찾을 수 없습니다.",
            style = AppFonts.size16Body1
        )
    }
}

// --- 비밀번호 찾기 UI ---
@Composable
private fun FindPasswordContent(
    id: String,
    onIdChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    isCodeSent: Boolean,
    onSendCodeClick: () -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    isVerified: Boolean,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newPasswordConfirm: String,
    onNewPasswordConfirmChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit,
    timerSeconds: Int,
    //focusManager: FocusManager
) {
    if (!isVerified) {
        // --- 인증 전 ---
        // ✅ Column에서 modifier = Modifier.pointerInput(Unit) { ... } 부분을 완전히 제거했습니다.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("아이디와 이메일을 입력해주세요.", style = AppFonts.size14Body2)
            OutlinedTextField(
                value = id,
                onValueChange = onIdChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("아이디 입력") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white,
                    unfocusedContainerColor = AppColors.white,
                    focusedIndicatorColor = AppColors.point,
                    unfocusedIndicatorColor = AppColors.main
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("이메일 주소 입력") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white,
                    unfocusedContainerColor = AppColors.white,
                    focusedIndicatorColor = AppColors.point,
                    unfocusedIndicatorColor = AppColors.main
                ),
                isError = email.isNotBlank() && !email.isEmailValid(),
                supportingText = {
                    if (email.isNotBlank() && !email.isEmailValid()) {
                        Text(
                            text = "올바른 이메일 형식을 입력해주세요.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
//                // ✅ 키보드 액션을 focusManager.clearFocus()로 통일
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                keyboardActions = KeyboardActions(
//                    onDone = { focusManager.clearFocus() }
//                ),
                singleLine = true
            )

            // (이하 버튼 및 다른 UI 로직은 이전과 동일)
            if (id.isNotBlank() && email.isNotBlank()) {
                val isTimerRunning = timerSeconds > 0
                Button(
                    onClick = onSendCodeClick,
                    enabled = !isTimerRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.main,
                        contentColor = AppColors.white,
                        disabledContainerColor = AppColors.light3Gray,
                        disabledContentColor = AppColors.light5Gray
                    )
                ) {
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    val timerText = String.format("%02d:%02d", minutes, seconds)
                    Text(if (isCodeSent) (if (isTimerRunning) "인증번호 재발송 ($timerText)" else "인증번호 재발송") else "인증번호 발송")
                }
            }
            if (isCodeSent) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("인증번호 입력") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.white,
                        unfocusedContainerColor = AppColors.white,
                        focusedIndicatorColor = AppColors.point,
                        unfocusedIndicatorColor = AppColors.main
                    ),
                    // ✅✅✅ 핵심 수정: 키보드 타입을 숫자 전용으로 변경 ✅✅✅
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(
//                        onDone = { focusManager.clearFocus() }
//                    ),
                    singleLine = true
                )
                if (code.isNotBlank()) {
                    Button(
                        onClick = onVerifyCodeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.main,
                            contentColor = AppColors.white,
                            disabledContainerColor = AppColors.light3Gray,
                            disabledContentColor = AppColors.light5Gray
                        )
                    ) { Text("인증번호 확인") }
                }
            }
        }
    } else {
        // --- 인증 후 (비밀번호 재설정) ---
        // ✅✅✅ 핵심 수정: 실시간 유효성 검사 UI 적용
        val isPasswordValid = newPassword.isPasswordValid()
        val isConfirmValid = newPassword == newPasswordConfirm && newPasswordConfirm.isNotEmpty()
        // ✅ Column에서 modifier = Modifier.pointerInput(Unit) { ... } 부분을 완전히 제거했습니다.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("새로운 비밀번호를 입력해주세요.", style = AppFonts.size14Body2)
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("새 비밀번호 (영문, 숫자 포함 8~20자)") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white,
                    unfocusedContainerColor = AppColors.white,
                    focusedIndicatorColor = AppColors.point,
                    unfocusedIndicatorColor =  AppColors.main
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            // 유효성 안내 메시지
            if (newPassword.isNotEmpty()) {
                if (isPasswordValid) {
                    Text(
                        text = "사용 가능한 비밀번호입니다.",
                        color = Color(0xFF4CAF50), // 초록색
                        style = AppFonts.size12Caption1
                    )
                } else {
                    Text(
                        text = "8~20자의 영문 및 숫자를 혼합해 주세요.",
                        color = Color.Red,
                        style = AppFonts.size12Caption1
                    )
                }
            }

            // 새 비밀번호 확인 입력창
            OutlinedTextField(
                value = newPasswordConfirm,
                onValueChange = onNewPasswordConfirmChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("새 비밀번호 확인") },
                visualTransformation = PasswordVisualTransformation(),
                isError = newPasswordConfirm.isNotBlank() && !isConfirmValid, // 일치하지 않을 때 에러 표시
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white,
                    unfocusedContainerColor = AppColors.white,
                    focusedIndicatorColor = AppColors.point,
                    unfocusedIndicatorColor =  AppColors.main
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            if (newPasswordConfirm.isNotBlank() && !isConfirmValid) {
                Text(
                    text = "비밀번호가 일치하지 않습니다.",
                    color = Color.Red,
                    style = AppFonts.size12Caption1
                )
            }

            Button(
                onClick = onResetPasswordClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = newPassword.isNotBlank() && newPassword == newPasswordConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.main,
                    contentColor = AppColors.white,
                    disabledContainerColor = AppColors.light3Gray,
                    disabledContentColor = AppColors.light5Gray
                )
            ) {
                Text("비밀번호 재설정")
            }
        }
    }
}



