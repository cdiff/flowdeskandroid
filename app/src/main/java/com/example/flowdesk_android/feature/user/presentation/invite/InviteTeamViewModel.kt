package com.example.flowdesk_android.feature.user.presentation.invite

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.core.domain.model.Role
import com.example.flowdesk_android.core.domain.repository.RoleRepository
import com.example.flowdesk_android.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InviteTeamUiState {
    object Idle : InviteTeamUiState()
    object Loading : InviteTeamUiState()
}

sealed class InviteTeamEvent {
    object Success : InviteTeamEvent()
    data class Error(val message: String) : InviteTeamEvent()
}

@HiltViewModel
class InviteTeamViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<InviteTeamUiState>(InviteTeamUiState.Idle)
    val uiState: StateFlow<InviteTeamUiState> = _uiState.asStateFlow()

    private val _allRoles = MutableStateFlow<List<Role>>(emptyList())
    val allRoles: StateFlow<List<Role>> = _allRoles.asStateFlow()

    private val _event = Channel<InviteTeamEvent>()
    val event: Flow<InviteTeamEvent> = _event.receiveAsFlow()

    init {
        fetchRoles()
    }

    private fun fetchRoles() {
        viewModelScope.launch {
            roleRepository.getRoles().onSuccess { roles ->
                _allRoles.value = roles
            }
        }
    }

    fun inviteUser(
        userId: String,
        password: String,
        corpName: String,
        userName: String,
        userEmail: String,
        userTel: String,
        userHp: String,
        roleIds: List<Int>?
    ) {
        viewModelScope.launch {
            _uiState.value = InviteTeamUiState.Loading
            userRepository.createUser(
                userId = userId,
                password = password,
                corpName = corpName,
                userName = userName,
                userEmail = userEmail,
                userTel = userTel,
                userHp = userHp,
                roleIds = roleIds
            )
                .onSuccess {
                    _uiState.value = InviteTeamUiState.Idle
                    _event.send(InviteTeamEvent.Success)
                }
                .onFailure { exception ->
                    _uiState.value = InviteTeamUiState.Idle
                    _event.send(InviteTeamEvent.Error(exception.message ?: "초대 중 오류가 발생했습니다."))
                }
        }
    }
}
