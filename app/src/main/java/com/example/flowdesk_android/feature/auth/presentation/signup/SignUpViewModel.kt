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

/**
 * 회원가입 단계 정의
 * TENANT → COMPANY → ADMIN_NAME → EMAIL → PHONE → (Password Fragment로 이동)
 */
enum class SignUpStep(val titleText: String, val label: String, val hint: String, val descText: String? = null) {
    ADMIN_NAME(
        titleText = "관리자 이름을\n입력해주세요",
        label = "관리자 이름",
        hint = "이름"
    ),
    EMAIL(
        titleText = "이메일을\n입력해주세요",
        label = "이메일",
        hint = "example@naver.com"
    ),
    PHONE(
        titleText = "휴대폰 번호를\n입력해주세요",
        label = "휴대폰 번호",
        hint = "010-0000-0000"
    ),
    COMPANY(
        titleText = "업체명을\n입력해주세요",
        label = "업체명",
        hint = "업체명",
        descText = "서비스 내부에서 노출될 실제 한글/영문 회사명입니다. (예: 주식회사 플로우)"
    ),
    TENANT(
        titleText = "로그인에 사용할\n테넌트 식별자를 입력해주세요",
        label = "테넌트 식별자",
        hint = "my-company",
        descText = "로그인 시 회사명(테넌트) 칸에 고정 입력할 영문 고유 ID입니다. 영문 소문자, 숫자, 하이픈(-) 조합만 허용됩니다."
    )
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel() {

    // 현재 단계 (이름부터 가입 시작)
    private val _currentStep = MutableStateFlow(SignUpStep.ADMIN_NAME)
    val currentStep: StateFlow<SignUpStep> = _currentStep.asStateFlow()

    // 각 단계별 저장된 값
    var tenantName: String = ""
        private set
    var companyName: String = ""
        private set
    var adminName: String = ""
        private set
    var email: String = ""
        private set
    var phone: String = ""
        private set

    // 완료된 항목 목록 (label to value)
    private val _completedItems = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val completedItems: StateFlow<List<Pair<String, String>>> = _completedItems.asStateFlow()

    // UI 상태 (비밀번호 단계)
    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _event = Channel<String>()
    val successMessage: Flow<String> = _event.receiveAsFlow()

    /**
     * 현재 단계 값을 저장하고 다음 단계로 이동
     * @return true: 다음 단계로 이동, false: 모든 단계 완료 (→ Password Fragment로 이동)
     */
    fun nextStep(value: String): Boolean {
        when (_currentStep.value) {
            SignUpStep.ADMIN_NAME -> {
                adminName = value
                addCompletedItem(SignUpStep.ADMIN_NAME.label, value)
                _currentStep.value = SignUpStep.EMAIL
            }
            SignUpStep.EMAIL -> {
                email = value
                addCompletedItem(SignUpStep.EMAIL.label, value)
                _currentStep.value = SignUpStep.PHONE
            }
            SignUpStep.PHONE -> {
                phone = value
                addCompletedItem(SignUpStep.PHONE.label, value)
                _currentStep.value = SignUpStep.COMPANY
            }
            SignUpStep.COMPANY -> {
                companyName = value
                addCompletedItem(SignUpStep.COMPANY.label, value)
                _currentStep.value = SignUpStep.TENANT
            }
            SignUpStep.TENANT -> {
                tenantName = value
                // 마지막 단계이므로 완료 목록에 추가하지 않고 비밀번호 설정 화면으로 이동함
                return false
            }
        }
        return true
    }

    /**
     * 이전 단계로 되돌아가기
     * @return true: 이전 단계로 이동, false: 첫 단계 (→ popBackStack)
     */
    fun previousStep(): Boolean {
        val steps = SignUpStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        return if (currentIndex > 0) {
            // 완료 항목에서 마지막 항목 제거
            val updatedItems = _completedItems.value.toMutableList()
            if (updatedItems.isNotEmpty()) updatedItems.removeAt(updatedItems.lastIndex)
            _completedItems.value = updatedItems
            _currentStep.value = steps[currentIndex - 1]
            true
        } else {
            false
        }
    }

    /**
     * 특정 완료 단계로 복귀하여 수정할 수 있도록 이동
     */
    fun goToStep(targetStep: SignUpStep) {
        val steps = SignUpStep.entries
        val targetIndex = steps.indexOf(targetStep)
        val currentIndex = steps.indexOf(_currentStep.value)

        if (targetIndex >= currentIndex) return // 현재 또는 미래 단계로는 갈 수 없음

        _currentStep.value = targetStep

        val updatedItems = _completedItems.value.toMutableList()
        val removeCount = currentIndex - targetIndex
        repeat(removeCount) {
            if (updatedItems.isNotEmpty()) {
                updatedItems.removeAt(0) // 스택 상단(최신 항목) 제거
            }
        }
        _completedItems.value = updatedItems
    }

    /**
     * 특정 단계를 활성화(복귀)하지 않고 완료된 목록의 개별 값만 직접 갱신
     */
    fun updateCompletedItemValue(step: SignUpStep, newValue: String) {
        // 1. 개별 저장 값 업데이트
        when (step) {
            SignUpStep.TENANT -> tenantName = newValue
            SignUpStep.COMPANY -> companyName = newValue
            SignUpStep.ADMIN_NAME -> adminName = newValue
            SignUpStep.EMAIL -> email = newValue
            SignUpStep.PHONE -> phone = newValue
        }

        // 2. 완성된 항목 목록(completedItems) 중 해당 라벨에 맞는 값을 변경하여 발행
        val updatedList = _completedItems.value.map { pair ->
            if (pair.first == step.label) {
                Pair(pair.first, newValue)
            } else {
                pair
            }
        }
        _completedItems.value = updatedList
    }

    /** 완료 항목 추가 */
    private fun addCompletedItem(label: String, value: String) {
        val updated = _completedItems.value.toMutableList()
        updated.add(0, Pair(label, value)) // 최신 항목이 가장 위에 (스택 순서)
        _completedItems.value = updated
    }

    /** 회원가입 API 호출 (비밀번호 Fragment에서 호출) */
    fun signUp(password: String) {
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
