package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RoleListUiState {
    object Loading : RoleListUiState()
    object Empty : RoleListUiState()
    data class Success(val roles: List<Role>) : RoleListUiState()
    data class Error(val message: String) : RoleListUiState()
}

sealed class RoleListEvent {
    object RoleCreated : RoleListEvent()
    object RoleDeleted : RoleListEvent()
    object StatusToggled : RoleListEvent()
    data class Error(val message: String) : RoleListEvent()
}

@HiltViewModel
class RolesViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 전체 역할 목록 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rolesFlow: Flow<Result<List<Role>>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(emptyList())) // 로딩 상태 전이를 위해 발행
                val res = roleRepository.getRoles()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태 uiState
    val uiState: StateFlow<RoleListUiState> = rolesFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            RoleListUiState.Error(result.exceptionOrNull()?.message ?: "알 수 없는 오류")
        } else {
            result.fold(
                onSuccess = { roles ->
                    if (roles.isEmpty() && _refreshTrigger.value == 0) RoleListUiState.Loading
                    else if (roles.isEmpty()) RoleListUiState.Empty
                    else RoleListUiState.Success(roles)
                },
                onFailure = { e ->
                    RoleListUiState.Error(e.message ?: "알 수 없는 오류")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoleListUiState.Loading)

    // 4. 전체 역할 목록 캐시 StateFlow
    private val allRoles: StateFlow<List<Role>> = rolesFlow.map { result ->
        result.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 6. 실시간 필터링된 역할 목록
    val filteredRoles: StateFlow<List<Role>> = combine(allRoles, debouncedQuery) { roles, query ->
        if (query.isBlank()) {
            roles
        } else {
            roles.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                it.roleName.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = Channel<RoleListEvent>()
    val event: Flow<RoleListEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun createRole(roleName: String, displayName: String, description: String) {
        viewModelScope.launch {
            roleRepository.createRole(roleName, displayName, description)
                .onSuccess {
                    _event.send(RoleListEvent.RoleCreated)
                    triggerRefresh()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "역할 생성 실패")) }
        }
    }

    fun deleteRole(roleId: Int) {
        viewModelScope.launch {
            roleRepository.deleteRole(roleId)
                .onSuccess {
                    _event.send(RoleListEvent.RoleDeleted)
                    triggerRefresh()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "역할 삭제 실패")) }
        }
    }

    fun toggleStatus(roleId: Int, currentIsActive: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentIsActive
            roleRepository.toggleRoleStatus(roleId, newStatus)
                .onSuccess {
                    _event.send(RoleListEvent.StatusToggled)
                    triggerRefresh()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
