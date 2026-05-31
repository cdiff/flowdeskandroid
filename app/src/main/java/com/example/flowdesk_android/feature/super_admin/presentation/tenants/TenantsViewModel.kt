package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.usecase.CreateTenantUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.DeleteTenantUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.GetTenantsUseCase
import com.example.flowdesk_android.feature.super_admin.domain.usecase.UpdateTenantStatusUseCase
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
    private val getTenantsUseCase: GetTenantsUseCase,
    private val createTenantUseCase: CreateTenantUseCase,
    private val deleteTenantUseCase: DeleteTenantUseCase,
    private val updateTenantStatusUseCase: UpdateTenantStatusUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<TenantListUiState>(TenantListUiState.Loading)
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()

    private val _filteredTenants = MutableStateFlow<List<Tenant>>(emptyList())
    val filteredTenants: StateFlow<List<Tenant>> = _filteredTenants.asStateFlow()

    private var allTenants: List<Tenant> = emptyList()

    private val _event = Channel<TenantListEvent>()
    val event: Flow<TenantListEvent> = _event.receiveAsFlow()

    init { fetchTenants() }

    fun fetchTenants() {
        viewModelScope.launch {
            _uiState.value = TenantListUiState.Loading
            getTenantsUseCase()
                .onSuccess { tenants ->
                    allTenants = tenants
                    _filteredTenants.value = tenants
                    _uiState.value = if (tenants.isEmpty()) TenantListUiState.Empty
                                     else TenantListUiState.Success(tenants)
                }
                .onFailure { _uiState.value = TenantListUiState.Error(it.message ?: "오류 발생") }
        }
    }

    fun search(query: String) {
        _filteredTenants.value = if (query.isBlank()) allTenants
        else allTenants.filter {
            it.tenantName.contains(query, ignoreCase = true) ||
            it.displayName.contains(query, ignoreCase = true) ||
            (it.domain?.contains(query, ignoreCase = true) == true)
        }
    }

    fun createTenant(tenantName: String, displayName: String, domain: String) {
        viewModelScope.launch {
            createTenantUseCase(tenantName, displayName, domain)
                .onSuccess {
                    _event.send(TenantListEvent.TenantCreated)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "테넌트 생성 실패")) }
        }
    }

    fun deleteTenant(tenantId: Int) {
        viewModelScope.launch {
            deleteTenantUseCase(tenantId)
                .onSuccess {
                    _event.send(TenantListEvent.TenantDeleted)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "테넌트 삭제 실패")) }
        }
    }

    fun toggleStatus(tenant: Tenant) {
        val newIsActive = !tenant.isActive
        viewModelScope.launch {
            updateTenantStatusUseCase(tenant.tenantId, newIsActive)
                .onSuccess {
                    _event.send(TenantListEvent.StatusToggled)
                    fetchTenants()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
