package com.example.flowdesk_android.feature.role.presentation.list

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.role.domain.model.Role
import com.example.flowdesk_android.feature.role.domain.usecase.CreateRoleUseCase
import com.example.flowdesk_android.feature.role.domain.usecase.DeleteRoleUseCase
import com.example.flowdesk_android.feature.role.domain.usecase.GetRolesUseCase
import com.example.flowdesk_android.feature.role.domain.usecase.ToggleRoleStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val getRolesUseCase: GetRolesUseCase,
    private val createRoleUseCase: CreateRoleUseCase,
    private val deleteRoleUseCase: DeleteRoleUseCase,
    private val toggleRoleStatusUseCase: ToggleRoleStatusUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<RoleListUiState>(RoleListUiState.Loading)
    val uiState: StateFlow<RoleListUiState> = _uiState.asStateFlow()

    private val _filteredRoles = MutableStateFlow<List<Role>>(emptyList())
    val filteredRoles: StateFlow<List<Role>> = _filteredRoles.asStateFlow()

    private var allRoles: List<Role> = emptyList()

    private val _event = Channel<RoleListEvent>()
    val event: Flow<RoleListEvent> = _event.receiveAsFlow()

    fun fetchRoles() {
        viewModelScope.launch {
            _uiState.value = RoleListUiState.Loading
            getRolesUseCase()
                .onSuccess { roles ->
                    allRoles = roles
                    _filteredRoles.value = roles
                    _uiState.value = if (roles.isEmpty()) RoleListUiState.Empty
                                     else RoleListUiState.Success(roles)
                }
                .onFailure { _uiState.value = RoleListUiState.Error(it.message ?: "오류") }
        }
    }

    fun search(query: String) {
        _filteredRoles.value = if (query.isBlank()) allRoles
        else allRoles.filter {
            it.displayName.contains(query, ignoreCase = true) ||
            it.roleName.contains(query, ignoreCase = true) ||
            (it.description?.contains(query, ignoreCase = true) == true)
        }
    }

    fun createRole(roleName: String, displayName: String, description: String) {
        viewModelScope.launch {
            createRoleUseCase(roleName, displayName, description)
                .onSuccess {
                    _event.send(RoleListEvent.RoleCreated)
                    fetchRoles()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "역할 생성 실패")) }
        }
    }

    fun deleteRole(roleId: Int) {
        viewModelScope.launch {
            deleteRoleUseCase(roleId)
                .onSuccess {
                    _event.send(RoleListEvent.RoleDeleted)
                    fetchRoles()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "역할 삭제 실패")) }
        }
    }

    fun toggleStatus(roleId: Int, currentIsActive: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentIsActive
            toggleRoleStatusUseCase(roleId, newStatus)
                .onSuccess {
                    _event.send(RoleListEvent.StatusToggled)
                    fetchRoles()
                }
                .onFailure { _event.send(RoleListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }
}
