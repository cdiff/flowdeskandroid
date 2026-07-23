package com.example.flowdesk_android.feature.system_management.presentation.website

import com.example.flowdesk_android.feature.system_management.domain.model.Website

/**
 * 웹사이트 상세/수정 화면 UI 상태
 */
sealed class WebsiteDetailUiState {
    object Idle : WebsiteDetailUiState()
    object Loading : WebsiteDetailUiState()
    
    data class Success(
        val website: Website,
        val canUpdate: Boolean,
        val canDelete: Boolean
    ) : WebsiteDetailUiState()

    data class Error(val message: String) : WebsiteDetailUiState()
    
    object UpdateSuccess : WebsiteDetailUiState()
    
    object DeleteSuccess : WebsiteDetailUiState()
}
