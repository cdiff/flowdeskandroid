package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.CreateUserRequest
import com.example.flowdesk_android.data.remote.dto.RoleDto
import com.example.flowdesk_android.domain.usecase.CreateUserUseCase
import com.example.flowdesk_android.domain.usecase.GetRolesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InviteTeamState {
    object Idle : InviteTeamState()
    object Loading : InviteTeamState()
    object Success : InviteTeamState()
    data class Error(val message: String) : InviteTeamState()
}

@HiltViewModel
class InviteTeamViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val getRolesUseCase: GetRolesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<InviteTeamState>(InviteTeamState.Idle)
    val state: StateFlow<InviteTeamState> = _state

    private val _allRoles = MutableStateFlow<List<RoleDto>>(emptyList())
    val allRoles: StateFlow<List<RoleDto>> = _allRoles

    init {
        fetchRoles()
    }

    private fun fetchRoles() {
        viewModelScope.launch {
            val result = getRolesUseCase()
            if (result.isSuccess) {
                _allRoles.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun inviteUser(request: CreateUserRequest) {
        viewModelScope.launch {
            _state.value = InviteTeamState.Loading
            createUserUseCase(request)
                .onSuccess {
                    _state.value = InviteTeamState.Success
                }
                .onFailure { exception ->
                    _state.value = InviteTeamState.Error(exception.message ?: "초대 중 오류가 발생했습니다.")
                }
        }
    }
}
