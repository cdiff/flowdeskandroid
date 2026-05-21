package com.example.flowdesk_android.feature.role.presentation.permissions

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.core.domain.model.PermissionCatalog
import com.example.flowdesk_android.core.domain.model.RoleDetail
import com.example.flowdesk_android.core.domain.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ManagePermissionsUiState {
    object Loading : ManagePermissionsUiState()
    data class Success(
        val roleDetail: RoleDetail,
        val catalog: PermissionCatalog,
        val selectedIds: Set<Int>
    ) : ManagePermissionsUiState()
    data class Error(val message: String) : ManagePermissionsUiState()
}

sealed class ManagePermissionsEvent {
    object PermissionsSaved : ManagePermissionsEvent()
    object PermissionsCopied : ManagePermissionsEvent()
    object InfoUpdated : ManagePermissionsEvent()
    data class Error(val message: String) : ManagePermissionsEvent()
}

@HiltViewModel
class ManagePermissionsViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ManagePermissionsUiState>(ManagePermissionsUiState.Loading)
    val uiState: StateFlow<ManagePermissionsUiState> = _uiState.asStateFlow()

    private val _event = Channel<ManagePermissionsEvent>()
    val event: Flow<ManagePermissionsEvent> = _event.receiveAsFlow()

    private var currentRoleId: Int = -1
    private var originalPermissionIds: Set<Int> = emptySet()

    fun load(roleId: Int) {
        currentRoleId = roleId
        viewModelScope.launch {
            _uiState.value = ManagePermissionsUiState.Loading

            val roleResult = roleRepository.getRoleDetail(roleId)
            val catalogResult = roleRepository.getPermissionCatalog()

            if (roleResult.isFailure) {
                _uiState.value = ManagePermissionsUiState.Error(roleResult.exceptionOrNull()?.message ?: "역할 조회 실패")
                return@launch
            }
            if (catalogResult.isFailure) {
                _uiState.value = ManagePermissionsUiState.Error(catalogResult.exceptionOrNull()?.message ?: "카탈로그 조회 실패")
                return@launch
            }

            val role = roleResult.getOrNull()!!
            val catalog = catalogResult.getOrNull()!!

            // 현재 부여된 permissionId Set 추출
            val assignedIds = role.permissionsByPage
                .flatMap { it.permissions }
                .map { it.permissionId }
                .toSet()

            originalPermissionIds = assignedIds
            _uiState.value = ManagePermissionsUiState.Success(role, catalog, assignedIds)
        }
    }

    fun savePermissions(newSelectedIds: Set<Int>) {
        val toAdd = (newSelectedIds - originalPermissionIds).toList()
        val toRemove = (originalPermissionIds - newSelectedIds).toList()

        viewModelScope.launch {
            roleRepository.updateRolePermissions(currentRoleId, toAdd.ifEmpty { null }, toRemove.ifEmpty { null })
                .onSuccess {
                    _event.send(ManagePermissionsEvent.PermissionsSaved)
                    load(currentRoleId)
                }
                .onFailure { _event.send(ManagePermissionsEvent.Error(it.message ?: "권한 저장 실패")) }
        }
    }

    fun copyFromRole(sourceRoleId: Int) {
        viewModelScope.launch {
            roleRepository.copyRolePermissions(currentRoleId, sourceRoleId)
                .onSuccess {
                    _event.send(ManagePermissionsEvent.PermissionsCopied)
                    load(currentRoleId)
                }
                .onFailure { _event.send(ManagePermissionsEvent.Error(it.message ?: "권한 복사 실패")) }
        }
    }

    fun updateRoleInfo(roleName: String, displayName: String, description: String?) {
        viewModelScope.launch {
            roleRepository.updateRoleInfo(currentRoleId, roleName, displayName, description)
                .onSuccess {
                    _event.send(ManagePermissionsEvent.InfoUpdated)
                    load(currentRoleId)
                }
                .onFailure { _event.send(ManagePermissionsEvent.Error(it.message ?: "역할 정보 수정 실패")) }
        }
    }
}
