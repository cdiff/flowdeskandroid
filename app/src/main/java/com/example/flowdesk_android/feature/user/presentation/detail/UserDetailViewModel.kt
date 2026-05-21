package com.example.flowdesk_android.feature.user.presentation.detail

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.core.domain.model.UserDetail
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

// ── UiState ──────────────────────────────────────────────
sealed class UserDetailUiState {
    object Loading : UserDetailUiState()
    data class Success(val user: UserDetail) : UserDetailUiState()
    data class Error(val message: String) : UserDetailUiState()
}

// ── One-time Events ───────────────────────────────────────
sealed class UserDetailEvent {
    object StatusChanged : UserDetailEvent()
    object RolesChanged : UserDetailEvent()
    object PasswordChanged : UserDetailEvent()
    object TokensInvalidated : UserDetailEvent()
    object InfoUpdated : UserDetailEvent()
    data class Error(val message: String) : UserDetailEvent()
}

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    private val _event = Channel<UserDetailEvent>()
    val event: Flow<UserDetailEvent> = _event.receiveAsFlow()

    fun loadUserDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = UserDetailUiState.Loading
            userRepository.getUserDetail(id)
                .onSuccess { _uiState.value = UserDetailUiState.Success(it) }
                .onFailure { _uiState.value = UserDetailUiState.Error(it.message ?: "오류") }
        }
    }

    fun updateStatus(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            userRepository.updateUserStatus(id, isActive)
                .onSuccess { _event.send(UserDetailEvent.StatusChanged) }
                .onFailure { _event.send(UserDetailEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun updateRoles(id: Int, add: List<Int>?, remove: List<Int>?) {
        viewModelScope.launch {
            userRepository.updateUserRoles(id, add, remove)
                .onSuccess { _event.send(UserDetailEvent.RolesChanged) }
                .onFailure { _event.send(UserDetailEvent.Error(it.message ?: "역할 변경 실패")) }
        }
    }

    fun changePassword(id: Int, newPassword: String) {
        viewModelScope.launch {
            userRepository.adminChangePassword(id, newPassword)
                .onSuccess { _event.send(UserDetailEvent.PasswordChanged) }
                .onFailure { _event.send(UserDetailEvent.Error(it.message ?: "비밀번호 변경 실패")) }
        }
    }

    fun invalidateTokens(id: Int) {
        viewModelScope.launch {
            userRepository.invalidateUserTokens(id)
                .onSuccess { _event.send(UserDetailEvent.TokensInvalidated) }
                .onFailure { _event.send(UserDetailEvent.Error(it.message ?: "토큰 무효화 실패")) }
        }
    }

    fun updateInfo(
        id: Int, corpName: String?, userName: String?,
        userEmail: String?, userTel: String?, userHp: String?
    ) {
        viewModelScope.launch {
            userRepository.updateUser(id, corpName, userName, userEmail, userTel, userHp)
                .onSuccess { _event.send(UserDetailEvent.InfoUpdated) }
                .onFailure { _event.send(UserDetailEvent.Error(it.message ?: "정보 수정 실패")) }
        }
    }
}
