package com.foodkeeper.feature.kakaologin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.SignUpUseCase // TODO: 로그인 전용 SignInUseCase로 교체하는 것을 권장합니다.
import com.foodkeeper.core.ui.util.isEmailValid
import com.foodkeeper.core.ui.util.isPasswordValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ 아이디/비밀번호 찾기 탭 종류
enum class FindAccountTab {
    ID, PASSWORD
}

// ✅ 로그인/계정찾기 화면 단계
enum class AuthStep {
    ID_INPUT, PW_INPUT, FIND_ACCOUNT
}

// --- 상태(State) 정의 ---

// ✅ 아이디 찾기 상세 상태
data class FindIdState(
    val email: String = "",
    val code: String = "",
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val foundId: String? = null,
    val timerSeconds: Int = 0
)

// ✅ 비밀번호 찾기 상세 상태
data class FindPasswordState(
    val id: String = "",
    val email: String = "",
    val code: String = "",
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val newPassword: String = "",
    val newPasswordConfirm: String = "",
    val timerSeconds: Int = 0
)

// ✅ 계정 찾기 전체 상태 (탭 + 각 탭의 상세 상태)
data class FindAccountUiState(
    val selectedTab: FindAccountTab = FindAccountTab.ID,
    val findIdState: FindIdState = FindIdState(),
    val findPasswordState: FindPasswordState = FindPasswordState()
)

// ✅ 로그인 화면의 최상위 상태
data class SignInUiState(
    val userId: String = "",
    val userPw: String = "",
    val currentStep: AuthStep = AuthStep.ID_INPUT,
    val isLoading: Boolean = false,
    val isSignInSuccess: Boolean = false,
    val errorMessage: String? = null,
    val findAccountState: FindAccountUiState = FindAccountUiState(),
    val resetPasswordSuccess: Boolean = false // ✅ 비밀번호 재설정 성공 상태 추가
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    // --- 일반 로그인 관련 함수 ---

    fun updateId(id: String) { _uiState.update { it.copy(userId = id) } }
    fun updatePw(pw: String) { _uiState.update { it.copy(userPw = pw) } }

    /**
     * 화면 단계를 변경합니다 (ID_INPUT, PW_INPUT, FIND_ACCOUNT).
     */
    fun setStep(step: AuthStep) { // ✅ 파라미터 타입 AuthStep으로 변경
        timerJob?.cancel() // 화면 단계 변경 시 타이머 취소
        _uiState.update { it.copy(currentStep = step) }
    }
    private fun startTimer(tab: FindAccountTab) {
        timerJob?.cancel()
        timerJob=viewModelScope.launch {
            val initialTime = 300 // 5분 = 300초

            // 타이머 시작 시 초기 시간 설정
            if (tab == FindAccountTab.ID) {
                _uiState.update {
                    it.copy(findAccountState = it.findAccountState.copy(
                        findIdState = it.findAccountState.findIdState.copy(timerSeconds = initialTime)
                    ))
                }
            } else {
                _uiState.update {
                    it.copy(findAccountState = it.findAccountState.copy(
                        findPasswordState = it.findAccountState.findPasswordState.copy(timerSeconds = initialTime)
                    ))
                }
            }

            // 1초마다 시간 감소
            for (i in initialTime downTo 1) {
                delay(1000)
                if (tab == FindAccountTab.ID) {
                    _uiState.update {
                        val newTime = it.findAccountState.findIdState.timerSeconds - 1
                        it.copy(findAccountState = it.findAccountState.copy(
                            findIdState = it.findAccountState.findIdState.copy(timerSeconds = newTime)
                        ))
                    }
                } else {
                    _uiState.update {
                        val newTime = it.findAccountState.findPasswordState.timerSeconds - 1
                        it.copy(findAccountState = it.findAccountState.copy(
                            findPasswordState = it.findAccountState.findPasswordState.copy(timerSeconds = newTime)
                        ))
                    }
                }
            }
        }
    }
    /**
     * 최종 로그인 로직
     */
    fun signIn() {
        val state = _uiState.value
        if (state.userId.isBlank() || state.userPw.isBlank()) return

        viewModelScope.launch {
            signInUseCase.signIn(state.userId, state.userPw)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "로그인에 실패했습니다.") }
                }
                .collect { res ->
                    // ✅ 조건 수정: accessToken이 비어있지 않으면 성공
                    if (!res.accessToken.isNullOrEmpty()) {
                        _uiState.update { it.copy(isSignInSuccess = true) }
                    } else {
                        _uiState.update { it.copy(errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.") }
                    }
                }
        }
    }

    // --- 계정 찾기 공통 함수 ---
    fun onTabSelected(tab: FindAccountTab) {
        // 탭 변경 시 모든 하위 상태 초기화
        _uiState.update {
            it.copy(findAccountState = FindAccountUiState(selectedTab = tab))
        }
    }

    // --- 아이디 찾기(Find ID) 관련 함수 ---
    fun onFindIdEmailChange(email: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findIdState = it.findAccountState.findIdState.copy(email = email)
                )
            )
        }
    }

    fun onFindIdCodeChange(code: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findIdState = it.findAccountState.findIdState.copy(code = code)
                )
            )
        }
    }

    fun sendFindIdCode() {
        val email = uiState.value.findAccountState.findIdState.email
        // ✅ 이메일 형식 검사 추가
        if (email.isBlank() || !email.isEmailValid()) {
            _uiState.update { it.copy(errorMessage = "올바른 이메일 형식을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            signInUseCase.verifyAccount(email) // UseCase가 이제 Flow<ApiResponse<String>>을 반환합니다.
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    // 네트워크 자체의 문제나 예상치 못한 에러 처리
                    _uiState.update { it.copy(errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다.") }
                }
                .collect { res -> // 'res'는 이제 ApiResponse<String> 타입입니다.
                    if (!res.isEmpty()) {
                        // 성공적으로 발송되었을 때의 로직
                        Log.d("AUTH", "아이디 찾기 인증번호 발송 성공: $email")
                        _uiState.update {
                            it.copy(
                                findAccountState = it.findAccountState.copy(
                                    findIdState = it.findAccountState.findIdState.copy(isCodeSent = true)
                                )
                            )
                        }
                        startTimer(FindAccountTab.ID) // 타이머 시작!
                    } else {
                        // API는 성공했으나, 서버 비즈니스 로직상 실패 (예: 가입되지 않은 이메일)
                        _uiState.update { it.copy(errorMessage = "인증번호 발송에 실패했습니다.") }
                    }
                }
        }
    }


    fun verifyFindIdCode() {
        val state = uiState.value.findAccountState.findIdState
        if (state.email.isBlank() || state.code.isBlank()) return

        viewModelScope.launch {
            signInUseCase.verifyAccountCode(state.email, state.code) // Flow<ApiResponse<AccountResponseDTO>> 반환
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다.") }
                }
                .collect { res -> // 'res'는 이제 ApiResponse<AccountResponseDTO> 타입입니다.
                    // ✅ 서버 응답의 result와 data를 모두 확인
                    if (!res.isEmpty()) {
                        // 성공 처리
                        _uiState.update {
                            it.copy(
                                findAccountState = it.findAccountState.copy(
                                    findIdState = it.findAccountState.findIdState.copy(
                                        isVerified = true,
                                        //foundId = res.data.account // ✅ 실제 찾은 아이디를 상태에 저장
                                    )
                                )
                            )
                        }
                    } else {
                        // 실패 처리
                        _uiState.update { it.copy(errorMessage = "인증번호가 일치하지 않습니다.") }
                    }
                }
        }
    }



    // --- 비밀번호 찾기(Find Password) 관련 함수 ---
    fun onFindPwIdChange(id: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findPasswordState = it.findAccountState.findPasswordState.copy(id = id)
                )
            )
        }
    }

    fun onFindPwEmailChange(email: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findPasswordState = it.findAccountState.findPasswordState.copy(email = email)
                )
            )
        }
    }

    fun onFindPwCodeChange(code: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findPasswordState = it.findAccountState.findPasswordState.copy(code = code)
                )
            )
        }
    }

    fun onNewPasswordChange(pw: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findPasswordState = it.findAccountState.findPasswordState.copy(newPassword = pw)
                )
            )
        }
    }

    fun onNewPasswordConfirmChange(pw: String) {
        _uiState.update {
            it.copy(
                findAccountState = it.findAccountState.copy(
                    findPasswordState = it.findAccountState.findPasswordState.copy(newPasswordConfirm = pw)
                )
            )
        }
    }

    // ✅✅✅ 핵심 수정 1: 비밀번호 찾기 인증번호 발송 API 연결
    fun sendFindPwCode() {
        val state = uiState.value.findAccountState.findPasswordState
        if (state.id.isBlank() || state.email.isBlank() || !state.email.isEmailValid()) {
            _uiState.update { it.copy(errorMessage = "아이디를 입력하고 올바른 이메일 형식을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            signInUseCase.verifyPassword(state.email, state.id)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다.") }
                }
                .collect { res ->
                    if (!res.isEmpty()) {
                        _uiState.update {
                            it.copy(findAccountState = it.findAccountState.copy(
                                findPasswordState = it.findAccountState.findPasswordState.copy(isCodeSent = true))
                            )
                        }
                        startTimer(FindAccountTab.PASSWORD) // 타이머 시작! (PASSWORD 탭으로 수정)
                    } else {
                        _uiState.update { it.copy(errorMessage = "일치하는 사용자 정보가 없습니다.") }
                    }
                }
        }
    }

    // ✅✅✅ 핵심 수정 2: 비밀번호 찾기 인증번호 확인 API 연결
    fun verifyFindPwCode() {
        val state = uiState.value.findAccountState.findPasswordState
        if (state.code.isBlank() || state.id.isBlank() || state.email.isBlank()) return

        viewModelScope.launch {
            signInUseCase.verifyPasswordCode(state.email, state.id, state.code)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다.") }
                }
                .collect { res ->
                    if (!res.isEmpty()) {
                        timerJob?.cancel() // 인증 성공 시 타이머 중지
                        _uiState.update {
                            it.copy(findAccountState = it.findAccountState.copy(
                                findPasswordState = it.findAccountState.findPasswordState.copy(isVerified = true))
                            )
                        }
                    } else {
                        _uiState.update { it.copy(errorMessage = "인증번호가 일치하지 않습니다.") }
                    }
                }
        }
    }

    // ✅✅✅ 핵심 수정 3: 비밀번호 재설정 API 연결
    fun resetPassword() {
        val state = uiState.value.findAccountState.findPasswordState
        if (state.newPassword != state.newPasswordConfirm || state.newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "새 비밀번호가 일치하지 않습니다.") }
            return
        }
        // ✅ 새로 추가된 유효성 검사 로직
        if (!state.newPassword.isPasswordValid()) {
            _uiState.update { it.copy(errorMessage = "비밀번호는 영문, 숫자를 포함하여 8~20자여야 합니다.") }
            return
        }
        if (!state.isVerified) {
            _uiState.update { it.copy(errorMessage = "이메일 인증이 완료되지 않았습니다.") }
            return
        }

        viewModelScope.launch {
           signInUseCase.resetPassword(state.email, state.id, state.newPassword)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다.") }
                }
                .collect { res ->
                    if (!res.isEmpty()) {
                        _uiState.update { it.copy(resetPasswordSuccess = true) } // 성공 상태 true로 변경
                        setStep(AuthStep.ID_INPUT) // 성공 시 로그인 화면으로 전환
                    } else {
                        _uiState.update { it.copy(errorMessage = "비밀번호 재설정에 실패했습니다. 다시 시도해주세요.") }
                    }
                }
        }
    }

    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }}
