package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.UserDetailDto
import com.example.flowdesk_android.domain.usecase.GetUserDetailUseCase
import com.example.flowdesk_android.domain.usecase.UpdateUserStatusUseCase
import com.example.flowdesk_android.domain.usecase.UpdateUserRolesUseCase
import com.example.flowdesk_android.domain.usecase.AdminChangePasswordUseCase
import com.example.flowdesk_android.domain.usecase.InvalidateUserTokensUseCase
import com.example.flowdesk_android.domain.usecase.UpdateUserUseCase
import com.example.flowdesk_android.data.remote.dto.UpdateUserInfoRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserDetailState {
    object Initial : UserDetailState()
    object Loading : UserDetailState()
    data class Success(val data: UserDetailDto) : UserDetailState()
    data class Error(val message: String) : UserDetailState()
    object StatusChangeSuccess : UserDetailState()
    object RoleChangeSuccess : UserDetailState()
    object PasswordChangeSuccess : UserDetailState()
    object TokenInvalidateSuccess : UserDetailState()
    object InfoChangeSuccess : UserDetailState()
}

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val updateUserRolesUseCase: UpdateUserRolesUseCase,
    private val adminChangePasswordUseCase: AdminChangePasswordUseCase,
    private val invalidateUserTokensUseCase: InvalidateUserTokensUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UserDetailState>(UserDetailState.Initial)
    val state: StateFlow<UserDetailState> = _state

    fun getUserDetail(id: Int) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = getUserDetailUseCase(id)
            if (result.isSuccess) {
                _state.value = UserDetailState.Success(result.getOrNull()!!)
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun updateUserStatus(id: Int, isActive: Int) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = updateUserStatusUseCase(id, isActive)
            if (result.isSuccess) {
                _state.value = UserDetailState.StatusChangeSuccess
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun updateUserRoles(id: Int, roleIds: List<Int>) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = updateUserRolesUseCase(id, roleIds)
            if (result.isSuccess) {
                _state.value = UserDetailState.RoleChangeSuccess
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun adminChangePassword(id: Int, newPassword: String) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = adminChangePasswordUseCase(id, newPassword)
            if (result.isSuccess) {
                _state.value = UserDetailState.PasswordChangeSuccess
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun invalidateTokens(id: Int) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = invalidateUserTokensUseCase(id)
            if (result.isSuccess) {
                _state.value = UserDetailState.TokenInvalidateSuccess
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun updateUser(id: Int, request: UpdateUserInfoRequest) {
        _state.value = UserDetailState.Loading
        viewModelScope.launch {
            val result = updateUserUseCase(id, request)
            if (result.isSuccess) {
                _state.value = UserDetailState.InfoChangeSuccess
            } else {
                _state.value = UserDetailState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _state.value = UserDetailState.Initial
    }
}
