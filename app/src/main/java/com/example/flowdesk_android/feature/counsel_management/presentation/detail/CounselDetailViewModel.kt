package com.example.flowdesk_android.feature.counsel_management.presentation.detail

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselStatusUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.data.dto.CounselUpdateRequest
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselStatusStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.EmployeeStat
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselMemo
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog
import com.example.flowdesk_android.feature.counsel_management.domain.repository.CounselRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

// ── UI State ───────────────────────────────────────────────────────────────────

sealed class CounselDetailUiState {
    object Loading : CounselDetailUiState()
    data class Success(val detail: CounselDetail) : CounselDetailUiState()
    data class Error(val message: String) : CounselDetailUiState()
}

sealed class CounselUpdateState {
    object Idle : CounselUpdateState()
    object Loading : CounselUpdateState()
    object Success : CounselUpdateState()
    data class Error(val message: String) : CounselUpdateState()
}

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class CounselDetailViewModel @Inject constructor(
    private val counselRepository: CounselRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 현재 로드할 상담 ID
    private val _counselIdState = MutableStateFlow<Int>(-1)

    // 3. [핵심] 선언형 UI 상태 파이프라인 (상세 정보 조회)
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CounselDetailUiState> = combine(_counselIdState, _refreshTrigger) { id, _ ->
        id
    }.flatMapLatest { id ->
        flow {
            if (id == -1) {
                emit(CounselDetailUiState.Loading)
                return@flow
            }
            emit(CounselDetailUiState.Loading)
            counselRepository.getCounselDetail(id)
                .onSuccess { detail -> emit(CounselDetailUiState.Success(detail)) }
                .onFailure { err ->
                    val msg = err.message ?: "상담 정보를 불러오지 못했습니다."
                    emit(CounselDetailUiState.Error(msg))
                    sendError(msg)
                }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CounselDetailUiState.Loading)

    // 4. 담당자 및 상태 목록 반응형 관찰 (대시보드 API 재활용)
    @OptIn(ExperimentalCoroutinesApi::class)
    val employeeList: StateFlow<List<EmployeeStat>> = _refreshTrigger
        .flatMapLatest {
            flow {
                counselRepository.getDashboard()
                    .onSuccess { emit(it.employeeStats) }
                    .onFailure { emit(emptyList<EmployeeStat>()) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val statusList: StateFlow<List<CounselStatusStat>> = _refreshTrigger
        .flatMapLatest {
            flow {
                counselRepository.getDashboard()
                    .onSuccess { emit(it.statusDistribution) }
                    .onFailure { emit(emptyList<CounselStatusStat>()) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. 메모 목록 반응형 관찰
    @OptIn(ExperimentalCoroutinesApi::class)
    val memoList: StateFlow<List<CounselMemo>> = combine(_counselIdState, _refreshTrigger) { id, _ ->
        id
    }.flatMapLatest { id ->
        flow {
            if (id == -1) {
                emit(emptyList<CounselMemo>())
                return@flow
            }
            counselRepository.getCounselMemos(id)
                .onSuccess { emit(it) }
                .onFailure { emit(emptyList<CounselMemo>()) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6. 이력 목록 반응형 관찰
    @OptIn(ExperimentalCoroutinesApi::class)
    val logList: StateFlow<List<CounselLog>> = combine(_counselIdState, _refreshTrigger) { id, _ ->
        id
    }.flatMapLatest { id ->
        flow {
            if (id == -1) {
                emit(emptyList<CounselLog>())
                return@flow
            }
            counselRepository.getCounselLogs(id)
                .onSuccess { emit(it) }
                .onFailure { emit(emptyList<CounselLog>()) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _updateState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val updateState: StateFlow<CounselUpdateState> = _updateState.asStateFlow()

    private val _statusUpdateState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val statusUpdateState: StateFlow<CounselUpdateState> = _statusUpdateState.asStateFlow()

    private val _deleteState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val deleteState: StateFlow<CounselUpdateState> = _deleteState.asStateFlow()

    // 메모 등록 상태
    private val _memoAddState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val memoAddState: StateFlow<CounselUpdateState> = _memoAddState.asStateFlow()

    fun init(id: Int) {
        if (_counselIdState.value == id) return
        _counselIdState.value = id
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun updateCounsel(request: CounselUpdateRequest) {
        val currentId = _counselIdState.value
        if (currentId == -1) return
        _updateState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            counselRepository.updateCounsel(currentId, request)
                .onSuccess {
                    _updateState.value = CounselUpdateState.Success
                    triggerRefresh() // 수정 후 최신 데이터 반응형 갱신
                }
                .onFailure { err ->
                    val msg = err.message ?: "상담 수정에 실패했습니다."
                    _updateState.value = CounselUpdateState.Error(msg)
                    sendError(msg)
                }
        }
    }

    fun updateCounselStatus(counselStat: Int, counselResvDtm: String? = null) {
        val currentId = _counselIdState.value
        if (currentId == -1) return
        _statusUpdateState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            val request = CounselStatusUpdateRequest(
                counselStat = counselStat,
                counselResvDtm = counselResvDtm
            )
            counselRepository.updateCounselStatus(currentId, request)
                .onSuccess {
                    _statusUpdateState.value = CounselUpdateState.Success
                    triggerRefresh() // 상태 변경 후 반응형 갱신
                }
                .onFailure { err ->
                    val msg = err.message ?: "상태 변경에 실패했습니다."
                    _statusUpdateState.value = CounselUpdateState.Error(msg)
                    sendError(msg)
                }
        }
    }

    fun resetUpdateState() {
        _updateState.value = CounselUpdateState.Idle
    }

    fun resetStatusUpdateState() {
        _statusUpdateState.value = CounselUpdateState.Idle
    }

    fun deleteCounsel() {
        val currentId = _counselIdState.value
        if (currentId == -1) return
        _deleteState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            counselRepository.deleteCounsel(currentId)
                .onSuccess {
                    _deleteState.value = CounselUpdateState.Success
                }
                .onFailure { err ->
                    val msg = err.message ?: "상담 삭제에 실패했습니다."
                    _deleteState.value = CounselUpdateState.Error(msg)
                    sendError(msg)
                }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = CounselUpdateState.Idle
    }

    fun addCounselMemo(memoText: String) {
        val currentId = _counselIdState.value
        if (currentId == -1) return
        _memoAddState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            counselRepository.addCounselMemo(currentId, memoText)
                .onSuccess {
                    _memoAddState.value = CounselUpdateState.Success
                    triggerRefresh() // 메모 등록 후 반응형 갱신
                }
                .onFailure { err ->
                    val msg = err.message ?: "메모 작성에 실패했습니다."
                    _memoAddState.value = CounselUpdateState.Error(msg)
                    sendError(msg)
                }
        }
    }

    fun resetMemoAddState() {
        _memoAddState.value = CounselUpdateState.Idle
    }
}
