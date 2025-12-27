package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.data.mapper.external.ProfileDTO
import com.foodkeeper.core.data.repository.UserRepository
import javax.inject.Inject

/**
 * 서버로부터 내 프로필 정보(닉네임, 이미지 URL)를 가져오는 유스케이스
 */
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // operator fun invoke를 사용하면 useCase() 처럼 함수처럼 호출할 수 있습니다.
    suspend operator fun invoke(): Result<ProfileDTO> {
        return userRepository.getMyProfile()
    }
}
