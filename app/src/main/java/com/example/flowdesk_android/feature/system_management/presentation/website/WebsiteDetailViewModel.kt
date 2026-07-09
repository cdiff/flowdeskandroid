package com.example.flowdesk_android.feature.system_management.presentation.website

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteDetailViewModel @Inject constructor(
    private val repository: WebsiteRepository,
    private val sessionManager: com.example.flowdesk_android.data.local.SessionManager
) : ViewModel() {

    private val _actionState = MutableStateFlow<WebsiteDetailUiState>(WebsiteDetailUiState.Idle)
    private val _loadedWebsite = MutableStateFlow<com.example.flowdesk_android.feature.system_management.domain.model.Website?>(null)

    val uiState: StateFlow<WebsiteDetailUiState> = combine(
        _actionState,
        _loadedWebsite,
        sessionManager.observePermission("websites.update"),
        sessionManager.observePermission("websites.delete")
    ) { action, website, canUpdate, canDelete ->
        when (action) {
            is WebsiteDetailUiState.UpdateSuccess -> WebsiteDetailUiState.UpdateSuccess
            is WebsiteDetailUiState.DeleteSuccess -> WebsiteDetailUiState.DeleteSuccess
            is WebsiteDetailUiState.Loading -> WebsiteDetailUiState.Loading
            is WebsiteDetailUiState.Error -> WebsiteDetailUiState.Error(action.message)
            else -> {
                if (website != null) {
                    WebsiteDetailUiState.Success(website, canUpdate, canDelete)
                } else {
                    WebsiteDetailUiState.Idle
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WebsiteDetailUiState.Idle
    )

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    /**
     * 웹사이트 상세정보 로드
     */
    fun loadWebsiteDetail(webCode: String) {
        viewModelScope.launch {
            _actionState.value = WebsiteDetailUiState.Loading
            _loadedWebsite.value = null
            repository.getWebsiteDetail(webCode).fold(
                onSuccess = { website ->
                    _loadedWebsite.value = website
                    _actionState.value = WebsiteDetailUiState.Idle
                },
                onFailure = { throwable ->
                    _actionState.value = WebsiteDetailUiState.Error(throwable.message ?: "상세 정보를 조회하는 데 실패했습니다.")
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
            _actionState.value = WebsiteDetailUiState.Loading
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
                    _actionState.value = WebsiteDetailUiState.UpdateSuccess
                },
                onFailure = { throwable ->
                    _actionState.value = WebsiteDetailUiState.Error(throwable.message ?: "수정에 실패했습니다.")
                }
            )
        }
    }

    /**
     * 웹사이트 삭제
     */
    fun deleteWebsite(webCode: String) {
        viewModelScope.launch {
            _actionState.value = WebsiteDetailUiState.Loading
            repository.deleteWebsite(webCode).fold(
                onSuccess = {
                    _actionState.value = WebsiteDetailUiState.DeleteSuccess
                },
                onFailure = { throwable ->
                    _actionState.value = WebsiteDetailUiState.Error(throwable.message ?: "삭제에 실패했습니다.")
                }
            )
        }
    }
}
