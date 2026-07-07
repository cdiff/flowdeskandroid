package com.example.flowdesk_android.feature.system_management.presentation.website

import com.example.flowdesk_android.feature.system_management.domain.model.Website

/**
 * 웹사이트 목록 화면 UI 상태 sealed class
 */
sealed class WebsiteListUiState {
    object Loading : WebsiteListUiState()
    
    data class Success(
        val websites: List<Website>,
        val totalCount: Int
    ) : WebsiteListUiState()
    
    data class Error(
        val message: String
    ) : WebsiteListUiState()
}
