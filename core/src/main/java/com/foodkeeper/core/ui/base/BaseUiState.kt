package com.foodkeeper.core.ui.base

sealed interface BaseUiState {

    data object Init : BaseUiState      // 첫 진입
    data object Loading : BaseUiState   // 로딩 중
    data object Content : BaseUiState   // 화면 표시

    data object Processing : BaseUiState   // 화면 표시 중 로딩화면이 필요한 경우

    data class ErrorState(
        val message: String?
    ) : BaseUiState
}