package com.foodkeeper.feature.kakaologin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodkeeper.core.data.mapper.request.SignUpRequestDTO
import com.foodkeeper.core.domain.usecase.SignUpUseCase
import com.foodkeeper.core.ui.util.isPasswordValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val currentStep: Int = 1,

    // 입력값
    val userId: String = "",
    val userPw: String = "",
    val userPwCheck: String = "",
    val email: String = "",
    val authCode: String = "",
    val nickname: String = "",
    val gender: String = "", // "MALE", "FEMALE"

    // 상태값 (중복확인 여부 등)
    val isIdConfirmed: Boolean = false,
    val isEmailSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isPwConfirmed: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpSuccess: Boolean?=false
)
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase // 나중에 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    // 비밀번호 정규식: 8~20자, 영문 및 숫자 혼합
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$")

    val uiState = _uiState.asStateFlow()

    // STEP 1: 아이디 업데이트 및 중복 체크
    fun updateId(id: String) {
        _uiState.update { it.copy(userId = id,) }
    }


    // 1. 아이디 중복 확인 호출부
    // STEP 1: 아이디 업데이트 및 중복 체크
    fun checkId() {
        val currentId = _uiState.value.userId
        if (currentId.isBlank()) return

        viewModelScope.launch {
            signUpUseCase.checkIdDuplicate(currentId)
                .onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }

                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false, // ✅ 에러 발생 시 로딩 해제
                        errorMessage = e.message ?: "연결에 실패했습니다."
                    ) }
                }
                .collect { data ->
                    if (!data.isDuplicated) {
                        _uiState.update { it.copy(
                            currentStep = 2,
                            isIdConfirmed = true,
                            isLoading = false // ✅ 성공 시 로딩 해제
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false, // ✅ 중복 시 로딩 해제
                            errorMessage = "이미 사용 중인 아이디입니다."
                        ) }
                    }
                }
        }
    }


    // STEP 2: 비밀번호 업데이트 및 일치 확인
    fun updatePw(pw: String) {
        _uiState.update { it.copy(userPw = pw,) }
    }

    fun updatePwCheck(pwCheck: String) {
        _uiState.update { it.copy(userPwCheck = pwCheck,) }
    }



    // 비밀번호 체크 완료 시 호출되는 함수 수정
    fun checkPasswordMatch() {
        val state = _uiState.value
        if (state.userPw.isPasswordValid() && state.userPw == state.userPwCheck) {
            _uiState.update { it.copy(currentStep = 3) } // 유효하고 일치하면 다음 단계로
        }
    }

    // STEP 3: 이메일 업데이트 및 인증
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email,) }
    }

    // 이메일 중복 확인 및 인증 코드 발송
    // 이메일 중복 확인 및 인증 코드 발송
    fun sendAuthCode() {
        val currentEmail = _uiState.value.email
        if (currentEmail.isBlank()) {
            Log.d("SIGNUP_DEBUG", "이메일이 비어있어 중복체크를 취소합니다.")
            return
        }

        viewModelScope.launch {
            signUpUseCase.sendEmailVerification(currentEmail)
                .onStart {
                    Log.d("SIGNUP_DEBUG", "이메일 전송 시작: $currentEmail")
                    _uiState.update { it.copy(isLoading = true,) }
                }
                .catch { e ->
                    Log.e("SIGNUP_DEBUG", "이메일 전송 중 예외 발생: ${e.message}")
                    _uiState.update { it.copy(isLoading = false,errorMessage = e.message?: "알 수 없는 에러가 발생했습니다.",) }
                }
                .collect { res ->
                    // ✅ 이 부분이 로그캣의 핵심입니다.
                    Log.d("SIGNUP_DEBUG", "Collect 진입성공! 전체 응답: $res")
                    //Log.d("SIGNUP_DEBUG", "서버 결과값(result): ${res.result}")

                    if (!res.isEmpty()) {
                        Log.d("SIGNUP_DEBUG", "성공 판정: UI 상태 업데이트 실행")
                        _uiState.update { it.copy(
                            isEmailSent = true,
                            isLoading = false
                        ) }
                    } else {
                        Log.d("SIGNUP_DEBUG", "실패 판정: SUCCESS가 아님")
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "이미 가입된 이메일이거나 발송에 실패했습니다.",
                        ) }
                    }
                }
        }
    }


    // 인증 코드 확인
    fun verifyCode(code: String) {
        val currentEmail = _uiState.value.email
        if (code.isBlank()) return

        viewModelScope.launch {
            signUpUseCase.verifyEmailCode(currentEmail, code)
                .onStart { _uiState.update { it.copy(isLoading = true,) } }
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message,isLoading = false) }
                }
                .collect { response->
                    if (!response.isEmpty()) {
                        // 인증 성공 시 다음 단계(닉네임)로 이동
                        _uiState.update { it.copy(
                            currentStep = 4,
                            isEmailVerified = true,
                            isLoading = false
                        ) }
                    } else {
                        _uiState.update { it.copy(errorMessage = "인증번호가 일치하지 않습니다.",isLoading = false) }
                    }
                }
        }
    }

    // STEP 4: 닉네임
    fun onNicknameComplete(nickname: String) {
        _uiState.update { it.copy(currentStep = 5, nickname = nickname,) }
    }

    // STEP 5: 성별 선택 및 최종 가입
    fun selectGender(gender: String) {
        _uiState.update { it.copy(gender = gender,) }
    }

    // ... 기존 코드 유지

    /**
     * 최종 회원가입 요청 함수
     */
    fun signUp() {
        val currentState = _uiState.value

        // 1. DTO 생성 (이미 잘 구현되어 있음)
        val signUpRequest = SignUpRequestDTO(
            account = currentState.userId,
            password = currentState.userPw,
            email = currentState.email,
            nickname = currentState.nickname,
            gender = currentState.gender
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            Log.d("SIGNUP_DEBUG", "회원가입 요청 데이터: $signUpRequest")

            signUpUseCase.signUp(signUpRequest)
                .onStart { /* 필요한 초기화 로직 */ }
                .catch { e ->
                    Log.e("SIGNUP_DEBUG", "회원가입 통신 에러: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false, // 로딩 해제 추가
                            errorMessage = e.message ?: "서버 연결에 실패했습니다."
                        )
                    }
                }
                .collect { response ->
                    Log.d("SIGNUP_DEBUG", "회원가입 서버 응답: $response")

                    if (response == "SUCCESS") {
                        // ✅ 회원가입 성공 처리
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSignUpSuccess = true, // 이제 Screen에서 이동함
                                errorMessage = null
                            )
                        }
                    } else {
                        // ✅ 실패 처리 (메시지 구체화)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "회원가입 처리 중 오류가 발생했습니다."
                            )
                        }
                    }
                }
        }
    }

    // SignUpViewModel 내부 - 에러 메시지 초기화 로직 수정
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) } // ✅ null로 명시적 변경
    }


    // 뒤로가기 처리 (토스 방식)
    fun onBackPressed() {
        _uiState.update {
            if (it.currentStep > 1) it.copy(currentStep = it.currentStep - 1,)
            else it
        }
    }
}