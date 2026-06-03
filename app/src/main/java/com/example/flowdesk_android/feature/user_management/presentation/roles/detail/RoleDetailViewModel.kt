package com.example.flowdesk_android.feature.user_management.presentation.roles.detail

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.RoleDetail
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RoleDetailUiState {
    object Loading : RoleDetailUiState()
    data class Success(val role: RoleDetail) : RoleDetailUiState()
    data class Error(val message: String) : RoleDetailUiState()
}

sealed class RoleDetailEvent {
    object StatusToggled : RoleDetailEvent()
    object InfoUpdated : RoleDetailEvent()
    object Deleted : RoleDetailEvent()
    data class Error(val message: String) : RoleDetailEvent()
}

@HiltViewModel
class RoleDetailViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<RoleDetailUiState>(RoleDetailUiState.Loading)
    val uiState: StateFlow<RoleDetailUiState> = _uiState.asStateFlow()

    private val _event = Channel<RoleDetailEvent>()
    val event: Flow<RoleDetailEvent> = _event.receiveAsFlow()

    fun loadRoleDetail(roleId: Int) {
        viewModelScope.launch {
            _uiState.value = RoleDetailUiState.Loading
            roleRepository.getRoleDetail(roleId)
                .onSuccess { _uiState.value = RoleDetailUiState.Success(it) }
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
}
