package com.example.flowdesk_android.feature.auth.presentation.signup

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _event = Channel<String>()
    val successMessage: Flow<String> = _event.receiveAsFlow()

    fun signUp(
        tenantName: String, companyName: String, adminName: String,
        email: String, phone: String, password: String
    ) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            signUpUseCase(tenantName, companyName, adminName, email, phone, password)
                .onSuccess {
                    _uiState.value = SignUpUiState.Success
                    _event.send(it)
                }
                .onFailure { _uiState.value = SignUpUiState.Error(it.message ?: "회원가입 실패") }
        }
    }
}
