package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.domain.usecase.GetRolesUseCase
import com.example.flowdesk_android.domain.usecase.CreateRoleUseCase
import com.example.flowdesk_android.domain.usecase.ToggleRoleStatusUseCase
import com.example.flowdesk_android.domain.usecase.DeleteRoleUseCase
import com.example.flowdesk_android.data.remote.dto.CreateRoleRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RolesState {
    object Idle : RolesState()
    object Loading : RolesState()
    data class Success(val roles: List<RoleDto>) : RolesState()
    data class Error(val message: String) : RolesState()
}

sealed class CreateRoleState {
    object Idle : CreateRoleState()
    object Loading : CreateRoleState()
    object Success : CreateRoleState()
    data class Error(val message: String) : CreateRoleState()
}

@HiltViewModel
class RolesViewModel @Inject constructor(
    private val getRolesUseCase: GetRolesUseCase,
    private val createRoleUseCase: CreateRoleUseCase,
    private val toggleRoleStatusUseCase: ToggleRoleStatusUseCase,
    private val deleteRoleUseCase: DeleteRoleUseCase
) : ViewModel() {

    private val _rolesState = MutableStateFlow<RolesState>(RolesState.Idle)
    val rolesState: StateFlow<RolesState> = _rolesState

    private val _filteredRoles = MutableStateFlow<List<RoleDto>>(emptyList())
    val filteredRoles: StateFlow<List<RoleDto>> = _filteredRoles

    private val _createRoleState = MutableStateFlow<CreateRoleState>(CreateRoleState.Idle)
    val createRoleState: StateFlow<CreateRoleState> = _createRoleState

    private var allRoles: List<RoleDto> = emptyList()

    fun fetchRoles() {
        viewModelScope.launch {
            _rolesState.value = RolesState.Loading
            getRolesUseCase().fold(
                onSuccess = { roles ->
                    allRoles = roles
                    _filteredRoles.value = roles
                    _rolesState.value = RolesState.Success(roles)
                },
                onFailure = { exception ->
                    _rolesState.value = RolesState.Error(exception.message ?: "Failed to load roles")
                }
            )
        }
    }

    fun searchRoles(query: String) {
        val filtered = if (query.isBlank()) {
            allRoles
        } else {
            allRoles.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                it.roleName.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        _filteredRoles.value = filtered
    }

    fun createRole(request: CreateRoleRequest) {
        viewModelScope.launch {
            _createRoleState.value = CreateRoleState.Loading
            createRoleUseCase(request).fold(
                onSuccess = {
                    _createRoleState.value = CreateRoleState.Success
                },
                onFailure = { error ->
                    _createRoleState.value = CreateRoleState.Error(error.message ?: "역할 생성에 실패했습니다.")
                }
            )
        }
    }

    fun resetCreateRoleState() {
        _createRoleState.value = CreateRoleState.Idle
    }

    fun toggleRoleStatus(id: Int, currentIsActive: Int) {
        val newIsActive = if (currentIsActive == 1) 0 else 1
        viewModelScope.launch {
            _rolesState.value = RolesState.Loading
            toggleRoleStatusUseCase(id, newIsActive).fold(
                onSuccess = {
                    fetchRoles()
                },
                onFailure = { error ->
                    _rolesState.value = RolesState.Error(error.message ?: "상태 변경에 실패했습니다.")
                }
            )
        }
    }

    fun deleteRole(id: Int) {
        viewModelScope.launch {
            _rolesState.value = RolesState.Loading
            deleteRoleUseCase(id).fold(
                onSuccess = {
                    fetchRoles()
                },
                onFailure = { error ->
                    _rolesState.value = RolesState.Error(error.message ?: "역할 삭제에 실패했습니다.")
                }
            )
        }
    }
}
