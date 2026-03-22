package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.RoleDetailResponse
import com.example.flowdesk_android.domain.usecase.GetRoleDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RoleDetailState {
    object Initial : RoleDetailState()
    object Loading : RoleDetailState()
    data class Success(val role: RoleDetailResponse) : RoleDetailState()
    data class Error(val message: String) : RoleDetailState()
}

@HiltViewModel
class RoleDetailViewModel @Inject constructor(
    private val getRoleDetailUseCase: GetRoleDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RoleDetailState>(RoleDetailState.Initial)
    val state: StateFlow<RoleDetailState> = _state.asStateFlow()

    fun fetchRoleDetail(roleId: Int) {
        viewModelScope.launch {
            _state.value = RoleDetailState.Loading
            val result = getRoleDetailUseCase(roleId)
            result.onSuccess { role ->
                _state.value = RoleDetailState.Success(role)
            }.onFailure { error ->
                _state.value = RoleDetailState.Error(error.message ?: "역할 상세 정보를 불러오는데 실패했습니다.")
            }
        }
    }
}
