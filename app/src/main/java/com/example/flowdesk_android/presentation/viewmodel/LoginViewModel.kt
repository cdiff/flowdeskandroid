package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.domain.model.User
import com.example.flowdesk_android.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(tenantName: String, userId: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = loginUseCase(tenantName, userId, password)
            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }
}
