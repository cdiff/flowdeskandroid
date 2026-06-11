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
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<CounselDetailUiState>(CounselDetailUiState.Loading)
    val uiState: StateFlow<CounselDetailUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val updateState: StateFlow<CounselUpdateState> = _updateState.asStateFlow()

    private val _statusUpdateState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val statusUpdateState: StateFlow<CounselUpdateState> = _statusUpdateState.asStateFlow()

    // 담당자 목록 (대시보드 API에서 재활용)
    private val _employeeList = MutableStateFlow<List<EmployeeStat>>(emptyList())
    val employeeList: StateFlow<List<EmployeeStat>> = _employeeList.asStateFlow()

    // 상태 목록 (대시보드 API에서 재활용)
    private val _statusList = MutableStateFlow<List<CounselStatusStat>>(emptyList())
    val statusList: StateFlow<List<CounselStatusStat>> = _statusList.asStateFlow()

    // 메모 목록 (전용 API 연동)
    private val _memoList = MutableStateFlow<List<CounselMemo>>(emptyList())
    val memoList: StateFlow<List<CounselMemo>> = _memoList.asStateFlow()

    // 메모 등록 상태
    private val _memoAddState = MutableStateFlow<CounselUpdateState>(CounselUpdateState.Idle)
    val memoAddState: StateFlow<CounselUpdateState> = _memoAddState.asStateFlow()

    // 이력 목록 (전용 API 연동)
    private val _logList = MutableStateFlow<List<CounselLog>>(emptyList())
    val logList: StateFlow<List<CounselLog>> = _logList.asStateFlow()
 
    private var counselId: Int = -1

    fun init(id: Int) {
        if (counselId == id) return
        counselId = id
        loadDetail()
        loadEmployeeAndStatusList()
        loadMemos()
        loadLogs()
    }

    fun loadDetail() {
        if (counselId == -1) return
        _uiState.value = CounselDetailUiState.Loading
        viewModelScope.launch {
            counselRepository.getCounselDetail(counselId)
                .onSuccess { detail ->
                    _uiState.value = CounselDetailUiState.Success(detail)
                }
                .onFailure { err ->
                    val msg = err.message ?: "상담 정보를 불러오지 못했습니다."
                    _uiState.value = CounselDetailUiState.Error(msg)
                    sendError(msg)
                }
        }
    }

    /** 대시보드 API의 employeeStats / statusDistribution 재활용 */
    fun loadEmployeeAndStatusList() {
        viewModelScope.launch {
            counselRepository.getDashboard()
                .onSuccess { dashboard ->
                    _employeeList.value = dashboard.employeeStats
                    _statusList.value = dashboard.statusDistribution
                }
                .onFailure {
                    // 실패해도 상세 화면 자체는 표시할 수 있으므로 silent fail
                }
        }
    }

    fun updateCounsel(request: CounselUpdateRequest) {
        if (counselId == -1) return
        _updateState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            counselRepository.updateCounsel(counselId, request)
                .onSuccess {
                    _updateState.value = CounselUpdateState.Success
                    loadDetail() // 수정 후 최신 데이터 반영
                }
                .onFailure { err ->
                    val msg = err.message ?: "상담 수정에 실패했습니다."
                    _updateState.value = CounselUpdateState.Error(msg)
                    sendError(msg)
                }
        }
    }

    fun updateCounselStatus(counselStat: Int, counselResvDtm: String? = null) {
        if (counselId == -1) return
        _statusUpdateState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            val request = CounselStatusUpdateRequest(
                counselStat = counselStat,
                counselResvDtm = counselResvDtm
            )
            counselRepository.updateCounselStatus(counselId, request)
                .onSuccess {
                    _statusUpdateState.value = CounselUpdateState.Success
                    loadDetail() // 상태 변경 후 최신 데이터 반영
                    loadLogs()   // 상태 변경 후 이력 정보 새로고침
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

    fun loadMemos() {
        if (counselId == -1) return
        viewModelScope.launch {
            counselRepository.getCounselMemos(counselId)
                .onSuccess { list ->
                    _memoList.value = list
                }
                .onFailure {
                    // silent fail
                }
        }
    }

    fun addCounselMemo(memoText: String) {
        if (counselId == -1) return
        _memoAddState.value = CounselUpdateState.Loading
        viewModelScope.launch {
            counselRepository.addCounselMemo(counselId, memoText)
                .onSuccess {
                    _memoAddState.value = CounselUpdateState.Success
                    loadMemos()
                    loadDetail() // 메모 등록 후 로그 이력 갱신 등을 위해 상세 로드 병행
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

    fun loadLogs() {
        if (counselId == -1) return
        viewModelScope.launch {
            counselRepository.getCounselLogs(counselId)
                .onSuccess { list ->
                    _logList.value = list
                }
                .onFailure {
                    // silent fail
                }
        }
    }
}
