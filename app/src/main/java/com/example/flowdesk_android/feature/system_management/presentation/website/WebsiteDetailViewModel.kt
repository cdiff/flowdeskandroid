package com.example.flowdesk_android.feature.system_management.presentation.website

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteDetailViewModel @Inject constructor(
    private val repository: WebsiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteDetailUiState>(WebsiteDetailUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    /**
     * 웹사이트 상세정보 로드
     */
    fun loadWebsiteDetail(webCode: String) {
        viewModelScope.launch {
            _uiState.value = WebsiteDetailUiState.Loading
            repository.getWebsiteDetail(webCode).fold(
                onSuccess = { website ->
                    _uiState.value = WebsiteDetailUiState.Success(website)
                },
                onFailure = { throwable ->
                    _uiState.value = WebsiteDetailUiState.Error(throwable.message ?: "상세 정보를 조회하는 데 실패했습니다.")
                }
            )
        }
    }

    /**
     * 웹사이트 상세정보 수정
     */
    fun updateWebsite(
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
            _uiState.value = WebsiteDetailUiState.Loading
            repository.updateWebsite(
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
                    _uiState.value = WebsiteDetailUiState.UpdateSuccess
                },
                onFailure = { throwable ->
                    _uiState.value = WebsiteDetailUiState.Error(throwable.message ?: "수정에 실패했습니다.")
                }
            )
        }
    }

    /**
     * 웹사이트 삭제
     */
    fun deleteWebsite(webCode: String) {
        viewModelScope.launch {
            _uiState.value = WebsiteDetailUiState.Loading
            repository.deleteWebsite(webCode).fold(
                onSuccess = {
                    _uiState.value = WebsiteDetailUiState.DeleteSuccess
                },
                onFailure = { throwable ->
                    _uiState.value = WebsiteDetailUiState.Error(throwable.message ?: "삭제에 실패했습니다.")
                }
            )
        }
    }
}
