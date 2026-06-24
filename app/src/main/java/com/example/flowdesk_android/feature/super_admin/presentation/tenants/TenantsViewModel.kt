package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────
sealed class TenantListUiState {
    object Loading : TenantListUiState()
    object Empty : TenantListUiState()
    data class Success(val tenants: List<Tenant>) : TenantListUiState()
    data class Error(val message: String) : TenantListUiState()
}

// ── One-shot Events ───────────────────────────────────────
sealed class TenantListEvent {
    object TenantCreated : TenantListEvent()
    object TenantDeleted : TenantListEvent()
    object StatusToggled : TenantListEvent()
    data class Error(val message: String) : TenantListEvent()
}

@HiltViewModel
class TenantsViewModel @Inject constructor(
    private val superRepository: SuperRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<TenantListUiState>(TenantListUiState.Loading)
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()

    private val _allTenants = MutableStateFlow<List<Tenant>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val filteredTenants: StateFlow<List<Tenant>> = combine(_allTenants, _searchQuery) { tenants, query ->
        if (query.isBlank()) {
            tenants
        } else {
            tenants.filter {
                it.tenantName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true) ||
                (it.domain?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _event = Channel<TenantListEvent>()
    val event: Flow<TenantListEvent> = _event.receiveAsFlow()

    init { fetchTenants() }

    fun fetchTenants() {
        viewModelScope.launch {
            _uiState.value = TenantListUiState.Loading
            superRepository.getTenants()
                .onSuccess { tenants ->
                    _allTenants.value = tenants
                    _uiState.value = if (tenants.isEmpty()) TenantListUiState.Empty
                                     else TenantListUiState.Success(tenants)
                }
                .onFailure { _uiState.value = TenantListUiState.Error(it.message ?: "조회 실패") }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun createTenant(tenantName: String, displayName: String, domain: String) {
        viewModelScope.launch {
            superRepository.createTenant(tenantName, displayName, domain)
                .onSuccess {
                    _event.send(TenantListEvent.TenantCreated)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun deleteTenant(tenantId: Int) {
        viewModelScope.launch {
            superRepository.deleteTenant(tenantId)
                .onSuccess {
                    _event.send(TenantListEvent.TenantDeleted)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "삭제 실패")) }
        }
    }

    fun toggleStatus(tenant: Tenant) {
        val newIsActive = !tenant.isActive
        viewModelScope.launch {
            superRepository.updateTenantStatus(tenant.tenantId, newIsActive)
                .onSuccess {
                    _event.send(TenantListEvent.StatusToggled)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
