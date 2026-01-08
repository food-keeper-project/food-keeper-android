package com.foodkeeper.core.domain.usecase

import android.accounts.Account
import com.foodkeeper.core.data.mapper.external.ApiResponse
import com.foodkeeper.core.data.mapper.external.AuthTokenDTO
import com.foodkeeper.core.data.mapper.external.respone.AccountResponseDTO
import com.foodkeeper.core.data.mapper.request.SignUpRequestDTO
import com.foodkeeper.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface SignUpUseCase {
    // 1. 아이디 중복 확인 (중복이면 true, 사용가능이면 false 반환)
    suspend fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO>

    // 2. 이메일 중복확인 및 인증번호 발송
    suspend fun sendEmailVerification(email: String): Flow<String>

    // 3. 이메일 인증번호 확인
    suspend fun verifyEmailCode(email: String, code: String): Flow<String>

    // 4. 최종 회원가입 요청
    suspend fun signUp(signUpInfo: SignUpRequestDTO): Flow<String>
    suspend fun signIn(userId: String, userPw: String, fcmToken: String?): Flow<AuthTokenDTO>
}

class DefaultSignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository // Repository 인터페이스 필요
) : SignUpUseCase {

    override suspend fun checkIdDuplicate(userId: String): Flow<AccountResponseDTO> {
        return authRepository.checkIdDuplicate(userId)
    }

    override suspend fun sendEmailVerification(email: String): Flow<String> {
        return authRepository.verifyEmail(email)
    }

    override suspend fun verifyEmailCode(email: String, code: String): Flow<String> {
        return authRepository.verifyEmailCode(email, code)
    }

    override suspend fun signUp(signUpInfo: SignUpRequestDTO): Flow<String> {
        return authRepository.signUp(signUpInfo.account, signUpInfo.password, signUpInfo.email, signUpInfo.nickname, signUpInfo.gender)
    }

    override suspend fun signIn(
        userId: String,
        userPw: String,
        fcmToken: String?
    ): Flow<AuthTokenDTO> {
        return authRepository.signIn(userId=userId,userPw=userPw,fcmToken=fcmToken)
    }
}

