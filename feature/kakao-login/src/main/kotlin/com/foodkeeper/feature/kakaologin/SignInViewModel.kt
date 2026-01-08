package com.foodkeeper.feature.kakaologin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.domain.usecase.SignUpUseCase // TODO: 로그인 전용 SignInUseCase로 교체하는 것을 권장합니다.
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ 계정 찾기 화면의 상세 상태를 관리
data class FindAccountUiState(
    val selectedTab: FindAccountTab = FindAccountTab.ID, // 현재 선택된 탭 (아이디/비밀번호)
    val findId: String = "", // ✅ '비밀번호 찾기' 시 사용할 아이디 필드 추가
    val email: String = "",
    val code: String = "",
    val isCodeSent: Boolean = false, // 인증번호 발송 여부
    val isCodeVerified: Boolean = false, // 인증번호 확인 여부
    val foundUserId: String? = null, // ✅ 아이디 찾기 성공 시 결과 저장
    val newPassword: String = "",
    val newPasswordConfirm: String = ""
)

// ✅ 탭 종류를 Enum으로 정의
enum class FindAccountTab {
    ID, PASSWORD
}

// ✅ 로그인/계정찾기 단계를 나타내는 Enum (SignInStep -> AuthStep으로 이름 변경)
enum class AuthStep {
    ID_INPUT, PW_INPUT, FIND_ACCOUNT
}

// ✅ 기존 SignInUiState에 FindAccountUiState 통합
data class SignInUiState(
    val userId: String = "",
    val userPw: String = "",
    val currentStep: AuthStep = AuthStep.ID_INPUT, // ✅ AuthStep으로 타입 변경
    val isLoading: Boolean = false,
    val isSignInSuccess: Boolean = false,
    val errorMessage: String? = null,
    val findAccountState: FindAccountUiState = FindAccountUiState() // 계정 찾기 상태 추가
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    // --- 일반 로그인 관련 함수 ---

    fun updateId(id: String) { _uiState.update { it.copy(userId = id) } }
    fun updatePw(pw: String) { _uiState.update { it.copy(userPw = pw) } }

    /**
     * 화면 단계를 변경합니다 (ID_INPUT, PW_INPUT, FIND_ACCOUNT).
     */
    fun setStep(step: AuthStep) { // ✅ 파라미터 타입 AuthStep으로 변경
        _uiState.update { it.copy(currentStep = step) }
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

    // --- 계정 찾기 관련 함수 ---

    /**
     * '아이디 찾기' / '비밀번호 찾기' 탭을 변경합니다.
     */
    fun onTabSelected(tab: FindAccountTab) {
        // 탭을 변경할 때 관련 상태 초기화
        _uiState.update {
            it.copy(
                findAccountState = FindAccountUiState(selectedTab = tab)
            )
        }
    }

    /**
     * '비밀번호 찾기' 시 아이디 입력값을 변경합니다.
     */
    fun onFindAccountIdChange(id: String) {
        _uiState.update { it.copy(findAccountState = it.findAccountState.copy(findId = id)) }
    }


    /**
     * 계정 찾기 화면의 이메일 입력값을 변경합니다.
     */
    fun onFindAccountEmailChange(email: String) {
        _uiState.update { it.copy(findAccountState = it.findAccountState.copy(email = email)) }
    }

    /**
     * 계정 찾기 화면의 인증코드 입력값을 변경합니다.
     */
    fun onFindAccountCodeChange(code: String) {
        _uiState.update { it.copy(findAccountState = it.findAccountState.copy(code = code)) }
    }

    /**
     * 계정 찾기를 위한 인증코드를 발송합니다.
     */
    fun sendFindAccountCode() {
        viewModelScope.launch {
            val findState = uiState.value.findAccountState
            // UseCase에 적절한 함수가 있다고 가정 (예: sendEmailVerification)
            // 실제 함수 이름은 UseCase에 맞게 수정해야 합니다.
            signInUseCase.sendEmailVerification(findState.email)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "인증번호 발송에 실패했습니다.") }
                }
                .collect { message -> // 성공 메시지 또는 결과를 처리
                    Log.d("AUTH", "인증번호 발송 성공: $message")
                    _uiState.update {
                        it.copy(
                            findAccountState = it.findAccountState.copy(isCodeSent = true),
                            errorMessage = message // 성공 메시지를 사용자에게 보여줄 수 있음
                        )
                    }
                }
        }
    }


    /**
     * 발송된 인증코드가 맞는지 확인합니다.
     */
    fun verifyFindAccountCode() {
        viewModelScope.launch {
            val findState = uiState.value.findAccountState
            // UseCase에 적절한 함수가 있다고 가정 (예: verifyEmailCode)
            // 실제 함수 이름은 UseCase에 맞게 수정해야 합니다.
            signInUseCase.verifyEmailCode(findState.email, findState.code)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "인증번호가 일치하지 않습니다.") }
                }
                .collect { message -> // 성공 시, 아이디 찾기라면 찾은 아이디를 포함할 수 있음
                    Log.d("AUTH", "인증번호 확인 성공: $message")

                    // 아이디 찾기인 경우, message에 찾은 아이디가 포함되어 있다고 가정
                    val foundId = if (findState.selectedTab == FindAccountTab.ID) message else null

                    _uiState.update {
                        it.copy(
                            findAccountState = it.findAccountState.copy(
                                isCodeVerified = true,
                                foundUserId = foundId
                            )
                        )
                    }
                }
        }
    }

    // ✅ 새 비밀번호 입력값 변경
    fun onNewPasswordChange(pw: String) {
        _uiState.update { it.copy(findAccountState = it.findAccountState.copy(newPassword = pw)) }
    }

    // ✅ 새 비밀번호 확인 입력값 변경
    fun onNewPasswordConfirmChange(pw: String) {
        _uiState.update { it.copy(findAccountState = it.findAccountState.copy(newPasswordConfirm = pw)) }
    }

    /**
     * 새 비밀번호로 재설정을 요청합니다.
     */
    fun resetPassword() {
        viewModelScope.launch {
            val findState = uiState.value.findAccountState
            if (findState.newPassword != findState.newPasswordConfirm || findState.newPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "비밀번호가 일치하지 않거나 유효하지 않습니다.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            // TODO: 비밀번호 재설정 API 호출 (id, email, newPassword 필요)
            _uiState.update { it.copy(isLoading = false) }
            // 성공 시 로그인 화면으로 돌려보내는 로직 등 추가 가능
        }
    }

    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
}
