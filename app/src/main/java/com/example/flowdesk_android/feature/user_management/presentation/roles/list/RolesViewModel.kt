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

    // 1. 상태 보관용 MutableStateFlow 정의
    private val _allRoles = MutableStateFlow<List<Role>>(emptyList())
    private val allRoles: StateFlow<List<Role>> = _allRoles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // 2. UI 로딩/에러/성공 상태 uiState 정의 (리액티브 결합)
    val uiState: StateFlow<RoleListUiState> = combine(
        _allRoles,
        _isLoading,
        _errorMessage
    ) { roles, loading, error ->
        if (error != null) {
            RoleListUiState.Error(error)
        } else if (loading && roles.isEmpty()) {
            RoleListUiState.Loading
        } else if (roles.isEmpty()) {
            RoleListUiState.Empty
        } else {
            RoleListUiState.Success(roles)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoleListUiState.Loading)

    // 3. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 4. 실시간 필터링된 역할 목록
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

    // 5. 서버로부터 전체 데이터 가져오기 (초기 진입/새로고침 시에만 사용)
    fun triggerRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            roleRepository.getRoles()
                .onSuccess { roles ->
                    _allRoles.value = roles
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "알 수 없는 오류"
                }
            _isLoading.value = false
        }
    }

    fun createRole(roleName: String, displayName: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            roleRepository.createRole(roleName, displayName, description)
                .onSuccess {
                    _event.send(RoleListEvent.RoleCreated)
                    triggerRefresh() // 생성의 경우 ID 정보 등을 받아와야 하므로 서버 갱신 수행
                }
                .onFailure {
                    _event.send(RoleListEvent.Error(it.message ?: "역할 생성 실패"))
                }
            _isLoading.value = false
        }
    }

    fun deleteRole(roleId: Int) {
        viewModelScope.launch {
            roleRepository.deleteRole(roleId)
                .onSuccess {
                    // 🚀 로컬 상태 직접 변경 (삭제 반영)
                    _allRoles.value = _allRoles.value.filter { it.roleId != roleId }
                    _event.send(RoleListEvent.RoleDeleted)
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "역할 삭제 실패")) }
        }
    }

    fun toggleStatus(roleId: Int, currentIsActive: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentIsActive
            roleRepository.toggleRoleStatus(roleId, newStatus)
                .onSuccess {
                    // 🚀 로컬 상태 직접 변경 (상태값 토글 반영)
                    _allRoles.value = _allRoles.value.map { role ->
                        if (role.roleId == roleId) {
                            role.copy(isActive = newStatus)
                        } else {
                            role
                        }
                    }
                    _event.send(RoleListEvent.StatusToggled)
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
