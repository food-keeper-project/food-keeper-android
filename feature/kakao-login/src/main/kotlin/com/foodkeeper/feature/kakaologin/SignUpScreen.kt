package com.foodkeeper.feature.kakaologin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.copy
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import com.foodkeeper.core.ui.util.isPasswordValid
//import androidx.compose.ui.text.intl.Locale
import kotlin.text.format
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackToLogin: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // ✅ 각 섹션을 위한 FocusRequester들
    // ✅ 1. 포커스 매니저 가져오기
    val focusManager = LocalFocusManager.current

    // ✅ 핵심: 시스템 뒤로가기 가로채기
    BackHandler {
        if (uiState.currentStep > 1) {
            viewModel.onBackPressed() // 이전 단계로 이동
        } else {
            onBackToLogin() // 첫 단계면 로그인 화면으로 이동
        }
    }
    // ✅ [핵심] 단계가 바뀔 때마다 실행되는 효과
    LaunchedEffect(uiState.currentStep) {
        // 키보드를 확실히 닫고 시작

        delay(300) // 애니메이션과 키보드 닫힘을 기다리는 시간

        // 스크롤 이동
        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)

    }
// ✅ 회원가입 성공 시 토스트 띄우고 로그인 화면으로 이동
    LaunchedEffect(uiState.isSignUpSuccess) {
        if (uiState.isSignUpSuccess == true) {
            Toast.makeText(context, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            onBackToLogin()
        }
    }

// ✅ 에러 메시지 감지 및 토스트 출력 로직
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // 메시지를 보여준 후 ViewModel의 에러 상태를 초기화해야
            // 동일한 에러가 발생했을 때 다시 토스트가 뜹니다.
            viewModel.clearErrorMessage()
        }
    }
    // ✅✅✅ 핵심 수정 1: 화면 전체를 Box로 감싸 로딩 UI를 추가합니다.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            containerColor = AppColors.white,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppColors.white
                    ),
                    title = {
                        Text(
                            text = "회원가입",
                            style = AppFonts.size22Title2,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            if (uiState.currentStep > 1) viewModel.onBackPressed()
                            else onBackToLogin()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .background(AppColors.white),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                // STEP 1: 아이디
                item {
                    IdSection(
                        isLoading = uiState.isLoading,
                        value = uiState.userId,
                        onValueChange = viewModel::updateId,
                        onCheckClick = viewModel::checkId,
                        isConfirmed = uiState.isIdConfirmed || uiState.currentStep > 1
                    )
                }

                // STEP 2: 비밀번호 & 재입력
                item {
                    AnimatedVisibility(visible = uiState.currentStep >= 2) {
                        PasswordSection(
                            pw = uiState.userPw,
                            pwCheck = uiState.userPwCheck,
                            onPwChange = viewModel::updatePw,
                            onPwCheckChange = viewModel::updatePwCheck,
                            onDone = viewModel::checkPasswordMatch,
                            isEnabled = uiState.currentStep == 2
                        )
                    }
                }

                // STEP 3: 이메일 & 인증번호
                item {
                    AnimatedVisibility(visible = uiState.currentStep >= 3) {
                        EmailSection(
                            email = uiState.email,
                            isSent = uiState.isEmailSent,
                            isVerified = uiState.isEmailVerified,
                            onSendClick = viewModel::sendAuthCode,
                            onVerifyClick = viewModel::verifyCode,
                            onEmailChange = viewModel::updateEmail,
                            isLoading = uiState.isLoading,
                            isEnabled = uiState.currentStep < 4 && !uiState.isEmailSent
                        )
                    }
                }

                // STEP 4: 닉네임
                item {
                    AnimatedVisibility(visible = uiState.currentStep >= 4) {
                        NicknameSection(
                            value = uiState.nickname,
                            onNext = viewModel::onNicknameComplete,
                            isEnabled = uiState.currentStep == 4
                        )
                    }
                }

                // STEP 5: 성별 및 가입버튼
                item {
                    AnimatedVisibility(visible = uiState.currentStep >= 5) {
                        GenderSection(
                            selectedGender = uiState.gender,
                            onSelect = viewModel::selectGender,
                            onSignUpClick = viewModel::signUp
                        )
                    }
                }
            }

            LaunchedEffect(uiState.currentStep) {
                scope.launch {
                    delay(100)
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
                }
            }
        }

        // ✅✅✅ 핵심 수정 2: isLoading 상태일 때 보여줄 전체 화면 로딩 UI
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // 반투명 배경
                    .pointerInput(Unit) { detectTapGestures { } }, // 터치 방지
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun IdSection(
    isLoading: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onCheckClick: () -> Unit,
    isConfirmed: Boolean,
    modifier: Modifier = Modifier
) {
    // ✅ 아이디 유효성 검사 (6자리 이상 12자리 이하)
    val isIdValid = value.length in 6..12

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("아이디", style = AppFonts.size16Body1)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier.weight(1f),
                placeholder = { Text("6~12자 아이디 입력") },
                enabled = !isConfirmed,
                isError = value.isNotEmpty() && !isIdValid, // 유효하지 않을 때 빨간 테두리
                singleLine = true,
                // ✅ 색상 커스텀 추가
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                    unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                    focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                    unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
                )
            )
            Button(
                onClick = onCheckClick,
                // ✅ 유효성 검사를 통과해야만 버튼 활성화
                enabled = isIdValid && !isConfirmed && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIdValid) AppColors.main else AppColors.light3Gray
                )
            ) {

                    Text(if (isConfirmed) "확인됨" else "중복확인")

            }
        }

        // ✅ 실시간 안내 문구
        if (value.isNotEmpty()) {
            if (isIdValid) {
                if (!isConfirmed) {
                    Text(
                        text = "사용 가능한 형식입니다. 중복확인을 해주세요.",
                        color = Color(0xFF4CAF50), // 초록색
                        style = AppFonts.size12Caption1
                    )
                }
            } else {
                Text(
                    text = "아이디는 6자 이상 12자 이하로 입력해주세요.",
                    color = Color.Red,
                    style = AppFonts.size12Caption1
                )
            }
        }
    }
}

// ✅ 비밀번호 무한 루프 해결 버전
@Composable
fun PasswordSection(
    modifier: Modifier = Modifier,
    pw: String,
    pwCheck: String,
    onPwChange: (String) -> Unit,
    onPwCheckChange: (String) -> Unit,
    onDone: () -> Unit,
    isEnabled: Boolean
) {
    // 유효성 검사 상태 (실시간 계산)
    // ✅✅✅ 핵심 수정: 하드코딩된 Regex 대신 isPasswordValid() 함수 사용
    val isPasswordValid = pw.isPasswordValid()
    val isConfirmValid = pw == pwCheck && pwCheck.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 비밀번호 입력
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("비밀번호", style = AppFonts.size16Body1)
            OutlinedTextField(
                enabled = isEnabled,
                value = pw,
                onValueChange = onPwChange,
                modifier = modifier.fillMaxWidth(),
                placeholder = { Text("영문/숫자 혼합, 8~20자") },
                visualTransformation = PasswordVisualTransformation(),
                isError = pw.isNotEmpty() && !isPasswordValid, // 조건 불충분 시 빨간 테두리
                singleLine = true,
                // ✅ 색상 커스텀 추가
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                    unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                    focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                    unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
                )
            )
            // 유효성 안내 메시지
            // ✅ 실시간 안내 문구 로직
            if (pw.isNotEmpty()) {
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
        }

        // 비밀번호 재입력
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("비밀번호 재입력", style = AppFonts.size16Body1)
            OutlinedTextField(
                enabled = isEnabled,
                value = pwCheck,
                onValueChange = onPwCheckChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("비밀번호 다시 입력") },
                visualTransformation = PasswordVisualTransformation(),
                isError = pwCheck.isNotEmpty() && !isConfirmValid,
                singleLine = true,
                // ✅✅✅ 핵심 수정: 키보드 '완료' 액션 추가 ✅✅✅
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // 유효할 때만 다음 단계로 진행하는 함수 호출
                    if (isPasswordValid && isConfirmValid) {
                        onDone()
                    }
                }),
                // ✅ 색상 커스텀 추가
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                    unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                    focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                    unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
                )
            )
            if (pwCheck.isNotEmpty() && !isConfirmValid) {
                Text(
                    text = if (pw != pwCheck) "비밀번호가 일치하지 않습니다." else "",
                    color = Color.Red,
                    style = AppFonts.size12Caption1
                )
            }
        }
    }
}

@Composable
fun EmailSection(
    modifier: Modifier = Modifier,
    email: String,
    isSent: Boolean, // ✅ 이 값이 true로 들어와야 함
    isVerified: Boolean,
    isLoading: Boolean,
    onSendClick: () -> Unit,
    onVerifyClick: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    isEnabled: Boolean
) {
    var code by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var timeLeft by remember { mutableIntStateOf(300) }
    val isTimerRunning = isSent && timeLeft > 0 && !isVerified

    // ✅ 발송 상태(isSent)가 true로 변하는 것을 감지
    LaunchedEffect(isSent) {
        if (isSent && !isVerified) {
            timeLeft = 300
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("이메일", style = AppFonts.size16Body1)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = modifier.weight(1f),
                    placeholder = { Text("example@foodkeeper.com") },
                    // ✅ 전체 흐름이 이메일 단계일 때만 활성화 (이미 인증 완료면 잠금)
                    enabled = isEnabled && !isVerified,
                    singleLine = true,
                    // ✅ 색상 커스텀 추가
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                        unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                        focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                        unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
                    )
                )
                Button(
                    onClick = {
                        onSendClick()
                    },
                    // ✅ 로딩 중이거나 타이머 작동 중이면 클릭 불가
                    enabled = isEnabled && email.isNotBlank() && !isTimerRunning && !isVerified && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) AppColors.light3Gray else AppColors.main
                    )
                ) {

                        Text(
                            text = when {
                                isVerified -> "인증 완료"
                                isTimerRunning -> "${timeLeft}초 후 재발송"
                                isSent -> "재발송"
                                else -> "인증번호"
                            }
                        )

                }
            }
        }

        // ✅ isSent가 true가 되면 이 부분이 나타나야 함
        AnimatedVisibility(
            visible = isSent && !isVerified,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("인증코드 입력") },
                        enabled = !isVerified,
                        singleLine = true,
                        trailingIcon = {
                            val minutes = timeLeft / 60
                            val seconds = timeLeft % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                color = if (timeLeft < 60) Color.Red else AppColors.main,
                                style = AppFonts.size12Caption1
                            )
                        },
                        // ✅ 색상 커스텀 추가
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                            unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                            focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                            unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
                        )
                    )
                    Button(
                        onClick = {
                            onVerifyClick(code)
                        },
                        enabled = code.length >= 4 && timeLeft > 0&& !isVerified
                    ) {
                        Text("확인")
                    }
                }
                if (timeLeft == 0) {
                    Text("인증 시간이 만료되었습니다. 다시 시도해주세요.", color = Color.Red, style = AppFonts.size12Caption1)
                }
            }
        }
    }
}

@Composable
fun NicknameSection(
    modifier: Modifier = Modifier,
    value: String,
    onNext: (String) -> Unit,
    isEnabled: Boolean
) {
    var text by remember { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("닉네임", style = AppFonts.size16Body1)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text("사용할 닉네임 입력 (필수)") },
            enabled = isEnabled,
            singleLine = true,
            // ✅ 색상 커스텀 추가
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.white, // 포커스 됐을 때 배경색
                unfocusedContainerColor = AppColors.white, // 포커스 아닐 때 배경색
                focusedIndicatorColor = AppColors.point, // 포커스 됐을 때 테두리 색
                unfocusedIndicatorColor =  AppColors.main // 포커스 아닐 때 테두리 색
            )
        )

        // ✅ 닉네임이 비어있으면 다음 단계로 못 가게 버튼 추가 (선택 사항)
        if (isEnabled) {
            Button(
                onClick = {
                    onNext(text)
                },
                modifier = Modifier.align(Alignment.End),
                enabled = text.isNotBlank() // ✅ 비어있으면 클릭 불가
            ) {
                Text("다음")
            }
        }
    }
}


@Composable
fun GenderSection(
    selectedGender: String,
    onSelect: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("성별", style = AppFonts.size16Body1)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderButton("남성", "M", selectedGender == "M", onSelect, Modifier.weight(1f))
                GenderButton("여성", "F", selectedGender == "F", onSelect, Modifier.weight(1f))
            }
        }

        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedGender.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.main)
        ) {
            Text("회원가입 완료", style = AppFonts.size16Body1)
        }
    }
}

@Composable
fun GenderButton(
    label: String,
    value: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedButton(
        onClick = { onSelect(value) },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) AppColors.main.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) AppColors.main else AppColors.black
        ),
        border = BorderStroke(1.dp, if (isSelected) AppColors.main else AppColors.light3Gray)
    ) {
        Text(label)
    }
}

