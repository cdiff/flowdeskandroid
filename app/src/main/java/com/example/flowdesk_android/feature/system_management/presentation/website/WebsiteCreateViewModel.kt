package com.example.flowdesk_android.feature.system_management.presentation.website

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsiteCreateUiState {
    object Idle : WebsiteCreateUiState()
    object Loading : WebsiteCreateUiState()
    object Success : WebsiteCreateUiState()
    data class Error(val message: String) : WebsiteCreateUiState()
}

@HiltViewModel
class WebsiteCreateViewModel @Inject constructor(
    private val repository: WebsiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteCreateUiState>(WebsiteCreateUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun createWebsite(
        webCode: String,
        userSeq: Int,
        webUrl: String,
        webTitle: String,
        webImg: String?,
        webDesc: String?,
        webMemo: String?,
        isActive: Boolean,
        duplicateAllowAfterDays: Int
    ) {
        viewModelScope.launch {
            _uiState.value = WebsiteCreateUiState.Loading
            repository.createWebsite(
                webCode = webCode,
                userSeq = userSeq,
                webUrl = webUrl,
                webTitle = webTitle,
                webImg = webImg,
                webDesc = webDesc,
                webMemo = webMemo,
                isActive = isActive,
                duplicateAllowAfterDays = duplicateAllowAfterDays
            ).fold(
                onSuccess = {
                    _uiState.value = WebsiteCreateUiState.Success
                },
                onFailure = { throwable ->
                    _uiState.value = WebsiteCreateUiState.Error(throwable.message ?: "등록에 실패했습니다.")
                }
            )
        }
    }
}
