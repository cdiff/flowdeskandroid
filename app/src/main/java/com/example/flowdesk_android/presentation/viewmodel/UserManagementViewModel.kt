package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.UserDto
import com.example.flowdesk_android.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UsersState {
    object Idle : UsersState()
    object Loading : UsersState()
    data class Success(val users: List<UserDto>) : UsersState()
    data class Error(val message: String) : UsersState()
}

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _usersState = MutableStateFlow<UsersState>(UsersState.Idle)
    val usersState: StateFlow<UsersState> = _usersState.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<UserDto>>(emptyList())
    val filteredUsers: StateFlow<List<UserDto>> = _filteredUsers.asStateFlow()

    private var allUsers: List<UserDto> = emptyList()

    fun fetchUsers() {
        viewModelScope.launch {
            _usersState.value = UsersState.Loading
            getUsersUseCase()
                .onSuccess { users ->
                    allUsers = users
                    _filteredUsers.value = users
                    _usersState.value = UsersState.Success(users)
                }
                .onFailure { exception ->
                    _usersState.value = UsersState.Error(exception.message ?: "Failed to load users")
                }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _filteredUsers.value = allUsers
        } else {
            val lowerQuery = query.lowercase()
            _filteredUsers.value = allUsers.filter {
                it.userName.lowercase().contains(lowerQuery) ||
                (it.userEmail?.lowercase()?.contains(lowerQuery) == true) ||
                it.userId.lowercase().contains(lowerQuery)
            }
        }
    }
}
