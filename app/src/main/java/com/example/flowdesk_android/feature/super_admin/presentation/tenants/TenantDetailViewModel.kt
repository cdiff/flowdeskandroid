package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.TenantDetail
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────
sealed class TenantDetailUiState {
    object Loading : TenantDetailUiState()
    data class Success(val tenant: TenantDetail) : TenantDetailUiState()
    data class Error(val message: String) : TenantDetailUiState()
}

// ── One-shot Events ───────────────────────────────────────
sealed class TenantDetailEvent {
    object SaveSuccess : TenantDetailEvent()
    data class Error(val message: String) : TenantDetailEvent()
}

@HiltViewModel
class TenantDetailViewModel @Inject constructor(
    private val superRepository: SuperRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<TenantDetailUiState>(TenantDetailUiState.Loading)
    val uiState: StateFlow<TenantDetailUiState> = _uiState.asStateFlow()

    private val _event = Channel<TenantDetailEvent>()
    val event: Flow<TenantDetailEvent> = _event.receiveAsFlow()

    // 저장 중 여부 (버튼 중복 클릭 방지)
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun fetchDetail(tenantId: Int) {
        viewModelScope.launch {
            _uiState.value = TenantDetailUiState.Loading
            superRepository.getTenantDetail(tenantId)
                .onSuccess { _uiState.value = TenantDetailUiState.Success(it) }
                .onFailure { _uiState.value = TenantDetailUiState.Error(it.message ?: "상세 조회 실패") }
        }
    }

    fun saveTenant(
        tenantId: Int,
        tenantName: String,
        displayName: String,
        domain: String
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            superRepository.updateTenant(
                tenantId   = tenantId,
                tenantName  = tenantName.trim().takeIf { it.isNotBlank() },
                displayName = displayName.trim().takeIf { it.isNotBlank() },
                domain      = domain.trim().takeIf { it.isNotBlank() },
                isActive    = null
            )
                .onSuccess { _event.send(TenantDetailEvent.SaveSuccess) }
                .onFailure { _event.send(TenantDetailEvent.Error(it.message ?: "수정 실패")) }
            _isSaving.value = false
        }
    }
}
