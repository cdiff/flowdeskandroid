package com.example.flowdesk_android.feature.user_management.presentation.users.list

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.feature.user_management.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

sealed class UserListUiState {
    object Loading : UserListUiState()
    object Empty : UserListUiState()
    data class Success(val users: List<User>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

sealed class UserListEvent {
    object TokensInvalidated : UserListEvent()
}

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers.asStateFlow()

    private val _event = Channel<UserListEvent>()
    val event: Flow<UserListEvent> = _event.receiveAsFlow()

    private var allUsers: List<User> = emptyList()

    init { fetchUsers() }

    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            userRepository.getUsers()
                .onSuccess { users ->
                    allUsers = users
                    _filteredUsers.value = users
                    _uiState.value = if (users.isEmpty()) UserListUiState.Empty
                                     else UserListUiState.Success(users)
                }
                .onFailure { e ->
                    _uiState.value = UserListUiState.Error(e.message ?: "알 수 없는 오류")
                    sendError(e.message ?: "알 수 없는 오류")
                }
        }
    }

    fun search(query: String) {
        _filteredUsers.value = if (query.isBlank()) {
            allUsers
        } else {
            val q = query.lowercase()
            allUsers.filter {
                it.userName.lowercase().contains(q) ||
                it.userId.lowercase().contains(q) ||
                (it.userEmail?.lowercase()?.contains(q) == true)
            }
        }
    }

    fun toggleUserStatus(userId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            userRepository.updateUserStatus(userId, !currentStatus)
                .onSuccess {
                    fetchUsers()
                }
                .onFailure { e ->
                    _uiState.value = UserListUiState.Success(allUsers)
                    sendError(e.message ?: "상태 변경 실패")
                }
        }
    }

    fun invalidateTokens(userId: Int) {
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            userRepository.invalidateUserTokens(userId)
                .onSuccess {
                    fetchUsers()
                    _event.send(UserListEvent.TokensInvalidated)
                }
                .onFailure { e ->
                    _uiState.value = UserListUiState.Success(allUsers)
                    sendError(e.message ?: "로그아웃 실패")
                }
        }
    }
}
