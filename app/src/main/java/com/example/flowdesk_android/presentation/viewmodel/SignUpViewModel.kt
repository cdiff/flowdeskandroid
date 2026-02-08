package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val message: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    fun signUp(companyName: String, adminName: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            val result = signUpUseCase(companyName, adminName, email, phone, password)
            result.onSuccess { message ->
                _signUpState.value = SignUpState.Success(message)
            }.onFailure { exception ->
                _signUpState.value = SignUpState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }
}
