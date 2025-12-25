package com.foodkeeper.core.domain.model

/**
 * 로그인 요청의 결과를 나타내는 sealed class.
 * 이 클래스는 어떤 특정 로그인 방식(카카오, 구글 등)에도 의존하지 않습니다.
 */
sealed class LoginResult {
    /**
     * 로그인 성공 시 상태. 성공의 증표로 받은 값(예: 액세스 토큰)을 포함합니다.
     * @param token 인증에 성공하여 발급된 토큰
     */
    data class Success(val accessToken: String) : LoginResult()

    /**
     * 로그인 실패 시 상태. 실패 원인을 담는 메시지를 포함할 수 있습니다.
     * @param message 사용자에게 보여줄 수 있는 오류 메시지
     */
    data class Failure(val message: String?) : LoginResult()

    /**
     * 사용자가 로그인을 의도적으로 취소했을 때의 상태.
     */
    data object Canceled : LoginResult()
}
