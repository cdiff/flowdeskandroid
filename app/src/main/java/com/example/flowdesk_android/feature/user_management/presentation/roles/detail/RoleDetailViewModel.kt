package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.RoleDetail
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import com.example.flowdesk_android.feature.user_management.domain.usecase.CopyRolePermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.flowdesk_android.feature.user_management.domain.model.Role

sealed class RoleDetailUiState {
    object Loading : RoleDetailUiState()
    data class Success(val role: RoleDetail, val templates: List<Role>) : RoleDetailUiState()
    data class Error(val message: String) : RoleDetailUiState()
}

sealed class RoleDetailEvent {
    object StatusToggled : RoleDetailEvent()
    object InfoUpdated : RoleDetailEvent()
    object Deleted : RoleDetailEvent()
    object PermissionsCopied : RoleDetailEvent()
    data class Error(val message: String) : RoleDetailEvent()
}

@HiltViewModel
class RoleDetailViewModel @Inject constructor(
    private val roleRepository: RoleRepository,
    private val copyRolePermissionsUseCase: CopyRolePermissionsUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<RoleDetailUiState>(RoleDetailUiState.Loading)
    val uiState: StateFlow<RoleDetailUiState> = _uiState.asStateFlow()

    private val _event = Channel<RoleDetailEvent>()
    val event: Flow<RoleDetailEvent> = _event.receiveAsFlow()

    fun loadRoleDetail(roleId: Int) {
        viewModelScope.launch {
            _uiState.value = RoleDetailUiState.Loading
            roleRepository.getRoleDetail(roleId)
                .onSuccess { roleDetail ->
                    // 상세 정보 가져오기 성공 시 복사 템플릿으로 쓸 전체 역할 목록 조회
                    roleRepository.getRoles()
                        .onSuccess { allRoles ->
                            // 자신을 제외한 역할 목록 전달
                            val templates = allRoles.filter { it.roleId != roleId }
                            _uiState.value = RoleDetailUiState.Success(roleDetail, templates)
                        }
                        .onFailure {
                            _uiState.value = RoleDetailUiState.Success(roleDetail, emptyList())
                        }
                }
                .onFailure { _uiState.value = RoleDetailUiState.Error(it.message ?: "조회 실패") }
        }
    }

    fun toggleStatus(roleId: Int, currentIsActive: Boolean) {
        val newStatus = !currentIsActive
        viewModelScope.launch {
            roleRepository.toggleRoleStatus(roleId, newStatus)
                .onSuccess {
                    _event.send(RoleDetailEvent.StatusToggled)
                    loadRoleDetail(roleId)
                }
                .onFailure { _event.send(RoleDetailEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun updateInfo(roleId: Int, roleName: String, displayName: String, description: String?) {
        viewModelScope.launch {
            roleRepository.updateRoleInfo(roleId, roleName, displayName, description)
                .onSuccess {
                    _event.send(RoleDetailEvent.InfoUpdated)
                    loadRoleDetail(roleId)
                }
                .onFailure { _event.send(RoleDetailEvent.Error(it.message ?: "수정 실패")) }
        }
    }

    fun deleteRole(roleId: Int) {
        viewModelScope.launch {
            roleRepository.deleteRole(roleId)
                .onSuccess { _event.send(RoleDetailEvent.Deleted) }
                .onFailure { _event.send(RoleDetailEvent.Error(it.message ?: "삭제 실패")) }
        }
    }

    fun copyRolePermissions(roleId: Int, sourceRoleId: Int) {
        viewModelScope.launch {
            _uiState.value = RoleDetailUiState.Loading
            copyRolePermissionsUseCase(roleId, sourceRoleId)
                .onSuccess {
                    _event.send(RoleDetailEvent.PermissionsCopied)
                    loadRoleDetail(roleId)
                }
                .onFailure {
                    _event.send(RoleDetailEvent.Error(it.message ?: "역할 복사 실패"))
                    loadRoleDetail(roleId)
                }
        }
    }
}
