package com.foodkeeper.core.ui.base

sealed interface BaseUiState {

    data object Init : BaseUiState      // 첫 진입
    data object Loading : BaseUiState   // 로딩 중
    data object Content : BaseUiState   // 화면 표시

    data class ErrorState(
        val message: String?
    ) : BaseUiState
}