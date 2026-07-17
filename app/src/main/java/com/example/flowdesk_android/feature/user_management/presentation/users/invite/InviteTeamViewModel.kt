package com.example.flowdesk_android.feature.user_management.presentation.users.invite

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.auth.domain.model.AuthSession
import com.example.flowdesk_android.feature.auth.domain.usecase.AuthenticateSessionUseCase
import com.example.flowdesk_android.feature.user_management.domain.model.Role
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import com.example.flowdesk_android.feature.user_management.domain.repository.UserRepository
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

/**
 * 팀원 초대 단계 정의 (회원가입 SignUpStep과 동일한 패턴)
 * USER_NAME → EMAIL → TEL → HP → (Password → Role Fragment로 이동)
 */
enum class InviteStep(
    val titleText: String,
    val label: String,
    val hint: String,
    val isOptional: Boolean = false,
    val descText: String? = null
) {
    USER_NAME(
        titleText = "초대할 팀원의\n이름을 입력해주세요",
        label = "이름",
        hint = "홍길동"
    ),
    EMAIL(
        titleText = "팀원의 이메일을\n입력해주세요",
        label = "이메일 (로그인 ID)",
        hint = "example@naver.com",
        descText = "이 이메일이 로그인 아이디로 사용됩니다."
    ),
    TEL(
        titleText = "전화번호를\n입력해주세요",
        label = "전화번호",
        hint = "02-1234-5678",
        isOptional = true
    ),
    HP(
        titleText = "휴대폰 번호를\n입력해주세요",
        label = "휴대폰 번호",
        hint = "010-0000-0000",
        isOptional = true
    )
}

@HiltViewModel
class InviteTeamViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val authSessionUseCase: AuthenticateSessionUseCase
) : BaseViewModel() {

    // ── 단계 관리 (SignUpViewModel 패턴) ──────────────────────────────────────

    private val _currentStep = MutableStateFlow(InviteStep.USER_NAME)
    val currentStep: StateFlow<InviteStep> = _currentStep.asStateFlow()

    // 단계별 저장 값
    var userName: String = ""
        private set
    var userEmail: String = ""
        private set
    var userTel: String = ""
        private set
    var userHp: String = ""
        private set

    // 완료된 항목 목록 (label to value)
    private val _completedItems = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val completedItems: StateFlow<List<Pair<String, String>>> = _completedItems.asStateFlow()

    /**
     * 현재 단계 값을 저장하고 다음 단계로 이동
     * @return true: 다음 단계로 이동, false: 모든 단계 완료 (→ PasswordFragment로 이동)
     */
    fun nextStep(value: String): Boolean {
        when (_currentStep.value) {
            InviteStep.USER_NAME -> {
                userName = value
                addCompletedItem(InviteStep.USER_NAME.label, value)
                _currentStep.value = InviteStep.EMAIL
            }
            InviteStep.EMAIL -> {
                userEmail = value
                addCompletedItem(InviteStep.EMAIL.label, value)
                _currentStep.value = InviteStep.TEL
            }
            InviteStep.TEL -> {
                userTel = value
                if (value.isNotBlank()) addCompletedItem(InviteStep.TEL.label, value)
                _currentStep.value = InviteStep.HP
            }
            InviteStep.HP -> {
                userHp = value
                // 마지막 단계 → PasswordFragment로 이동
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
        val steps = InviteStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        return if (currentIndex > 0) {
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
     * 완료된 항목의 값을 직접 갱신 (인라인 편집)
     */
    fun updateCompletedItemValue(step: InviteStep, newValue: String) {
        when (step) {
            InviteStep.USER_NAME -> userName = newValue
            InviteStep.EMAIL -> userEmail = newValue
            InviteStep.TEL -> userTel = newValue
            InviteStep.HP -> userHp = newValue
        }
        val updatedList = _completedItems.value.map { pair ->
            if (pair.first == step.label) Pair(pair.first, newValue) else pair
        }
        _completedItems.value = updatedList
    }

    /** 완료 항목 추가 */
    private fun addCompletedItem(label: String, value: String) {
        val updated = _completedItems.value.toMutableList()
        updated.add(0, Pair(label, value))
        _completedItems.value = updated
    }

    /** 상태 초기화 (재초대 등 재사용 시) */
    fun resetSteps() {
        _currentStep.value = InviteStep.USER_NAME
        _completedItems.value = emptyList()
        userName = ""; userEmail = ""; userTel = ""; userHp = ""
    }

    // ── 역할 목록 ──────────────────────────────────────────────────────────────

    private val _allRoles = MutableStateFlow<List<Role>>(emptyList())
    val allRoles: StateFlow<List<Role>> = _allRoles.asStateFlow()

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

    // ── API 호출 ───────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<InviteTeamUiState>(InviteTeamUiState.Idle)
    val uiState: StateFlow<InviteTeamUiState> = _uiState.asStateFlow()

    private val _event = Channel<InviteTeamEvent>()
    val event: Flow<InviteTeamEvent> = _event.receiveAsFlow()

    fun inviteUser(
        password: String,
        roleIds: List<Int>?
    ) {
        viewModelScope.launch {
            _uiState.value = InviteTeamUiState.Loading

            val currentSession = authSessionUseCase.sessionState.value
            val corpName = if (currentSession is AuthSession.Active) {
                currentSession.user.corpName
            } else {
                ""
            }

            userRepository.createUser(
                userId = userEmail,   // 이메일을 userId로 사용
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
