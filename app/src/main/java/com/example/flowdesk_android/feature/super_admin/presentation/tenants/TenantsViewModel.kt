package com.example.flowdesk_android.feature.super_admin.presentation.tenants

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Tenant
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 전체 테넌트 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val tenantsFlow: Flow<Result<List<Tenant>>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(emptyList())) // 로딩 상태 전이를 위해 발행
                val res = superRepository.getTenants()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태 uiState
    val uiState: StateFlow<TenantListUiState> = tenantsFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            TenantListUiState.Error(result.exceptionOrNull()?.message ?: "조회 실패")
        } else {
            result.fold(
                onSuccess = { tenants ->
                    if (tenants.isEmpty() && _refreshTrigger.value == 0) TenantListUiState.Loading
                    else if (tenants.isEmpty()) TenantListUiState.Empty
                    else TenantListUiState.Success(tenants)
                },
                onFailure = { e ->
                    TenantListUiState.Error(e.message ?: "조회 실패")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TenantListUiState.Loading)

    // 4. 전체 테넌트 캐시 StateFlow
    private val allTenants: StateFlow<List<Tenant>> = tenantsFlow.map { result ->
        result.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 6. 실시간 필터링된 테넌트 목록
    val filteredTenants: StateFlow<List<Tenant>> = combine(allTenants, debouncedQuery) { tenants, query ->
        if (query.isBlank()) {
            tenants
        } else {
            tenants.filter {
                it.tenantName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true) ||
                (it.domain?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = Channel<TenantListEvent>()
    val event: Flow<TenantListEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun createTenant(tenantName: String, displayName: String, domain: String) {
        viewModelScope.launch {
            superRepository.createTenant(tenantName, displayName, domain)
                .onSuccess {
                    _event.send(TenantListEvent.TenantCreated)
                    triggerRefresh()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun deleteTenant(tenantId: Int) {
        viewModelScope.launch {
            superRepository.deleteTenant(tenantId)
                .onSuccess {
                    _event.send(TenantListEvent.TenantDeleted)
                    triggerRefresh()
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
                    triggerRefresh()
                }
                .onFailure { _event.send(TenantListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
