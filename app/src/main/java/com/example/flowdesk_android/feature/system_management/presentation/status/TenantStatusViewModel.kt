package com.example.flowdesk_android.feature.system_management.presentation.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.feature.system_management.domain.model.TenantStatus
import com.example.flowdesk_android.feature.system_management.domain.repository.SystemManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantStatusViewModel @Inject constructor(
    private val repository: SystemManagementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGroup = MutableStateFlow("all")
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _statusGroups = MutableStateFlow<List<String>>(emptyList())
    val statusGroups = _statusGroups.asStateFlow()

    private val _filteredGroups = MutableStateFlow<List<com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup>>(emptyList())
    val filteredGroups: StateFlow<List<com.example.flowdesk_android.feature.system_management.domain.model.TenantStatusGroup>> = _filteredGroups.asStateFlow()

    // Dynamic stats flows
    private val _totalGroups = MutableStateFlow(0)
    val totalGroups = _totalGroups.asStateFlow()

    private val _totalStatuses = MutableStateFlow(0)
    val totalStatuses = _totalStatuses.asStateFlow()

    private val _activeStatuses = MutableStateFlow(0)
    val activeStatuses = _activeStatuses.asStateFlow()

    private val _inactiveStatuses = MutableStateFlow(0)
    val inactiveStatuses = _inactiveStatuses.asStateFlow()

    // Loading & Error feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _selectedStatusDetail = MutableStateFlow<TenantStatus?>(null)
    val selectedStatusDetail: StateFlow<TenantStatus?> = _selectedStatusDetail.asStateFlow()

    init {
        // searchQuery 또는 selectedGroup이 바뀔 때마다 자동으로 목록 리프레시
        viewModelScope.launch {
            combine(searchQuery, selectedGroup) { query, group ->
                Pair(query, group)
            }.collect { (query, group) ->
                fetchStatuses(query, group)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateGroup(group: String) {
        _selectedGroup.value = group
    }

    fun refresh() {
        viewModelScope.launch {
            fetchStatuses(searchQuery.value, selectedGroup.value)
        }
    }

    private suspend fun fetchStatuses(query: String, group: String) {
        _isLoading.value = true
        // 탭 상태 그룹을 API 쿼리 문자열로 변환 (전체는 null, 나머지는 동적 매핑값 그대로 전달)
        val apiGroupParam = if (group == "all") null else group

        repository.getTenantStatuses(
            statusGroup = apiGroupParam,
            isActive = null, // 전체 활성/비활성 통합 수집하여 하단 카운트 분배
            q = query.ifEmpty { null }
        ).onSuccess { response ->
            // 그룹 단위 데이터 그대로 발행
            _filteredGroups.value = response.groups

            // 전체("all") 조회를 성공했을 때만 서버에서 리턴한 statusGroup 목록을 동적으로 업데이트
            if (group == "all") {
                val groupsFromApi = response.groups.map { it.statusGroup }.distinct()
                _statusGroups.value = groupsFromApi
            }

            // 집계용 모든 아이템 병합 리스트
            val allItems = response.groups.flatMap { it.items }

            // Dynamic 집계 업데이트
            _totalGroups.value = response.groups.size
            _totalStatuses.value = allItems.size
            _activeStatuses.value = allItems.count { it.isActive }
            _inactiveStatuses.value = allItems.count { !it.isActive }
        }.onFailure { error ->
            _errorMessage.emit(error.message ?: "목록 로딩 중 오류가 발생했습니다.")
        }
        _isLoading.value = false
    }

    // 1. 상태 추가 (POST)
    fun createStatus(
        group: String,
        key: String,
        name: String,
        desc: String,
        color: String,
        sort: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createTenantStatus(
                statusGroup = group,
                statusKey = key,
                statusName = name,
                description = desc,
                color = color,
                sortOrder = sort,
                isActive = if (isActive) 1 else 0
            ).onSuccess {
                fetchStatuses(searchQuery.value, selectedGroup.value)
            }.onFailure { error ->
                _errorMessage.emit(error.message ?: "상태 추가 중 오류가 발생했습니다.")
            }
            _isLoading.value = false
        }
    }

    // 2. 상태 수정 (PATCH)
    fun updateStatus(
        id: Long,
        name: String,
        desc: String,
        color: String,
        sort: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateTenantStatus(
                id = id,
                statusName = name,
                description = desc,
                color = color,
                sortOrder = sort,
                isActive = if (isActive) 1 else 0
            ).onSuccess {
                fetchStatuses(searchQuery.value, selectedGroup.value)
            }.onFailure { error ->
                _errorMessage.emit(error.message ?: "상태 수정 중 오류가 발생했습니다.")
            }
            _isLoading.value = false
        }
    }

    // 3. 상태 삭제 (DELETE)
    fun deleteStatus(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTenantStatus(id)
                .onSuccess {
                    fetchStatuses(searchQuery.value, selectedGroup.value)
                }.onFailure { error ->
                    _errorMessage.emit(error.message ?: "상태 삭제 중 오류가 발생했습니다.")
                }
            _isLoading.value = false
        }
    }

    // 4. 상태 활성화 토글 (PATCH status)
    fun toggleStatusActive(id: Long, currentActive: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val targetActiveInt = if (currentActive) 0 else 1
            repository.updateTenantStatusActive(id, targetActiveInt)
                .onSuccess {
                    fetchStatuses(searchQuery.value, selectedGroup.value)
                }.onFailure { error ->
                    _errorMessage.emit(error.message ?: "활성 상태 변경 중 오류가 발생했습니다.")
                }
            _isLoading.value = false
        }
    }

    // 5. 상태 상세 로드
    fun loadStatusDetail(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTenantStatusDetail(id)
                .onSuccess { status ->
                    _selectedStatusDetail.value = status
                }.onFailure { error ->
                    _errorMessage.emit(error.message ?: "상태 상세 정보를 불러오지 못했습니다.")
                }
            _isLoading.value = false
        }
    }

    // 6. 상태 상세 초기화
    fun clearSelectedStatusDetail() {
        _selectedStatusDetail.value = null
    }

    // 정규식 및 중복 유효성 검증 비즈니스 로직
    private val keyRegex = Regex("^[a-z0-9_]+$")
    private val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")

    fun validateStatusKey(key: String): Boolean {
        return key.isNotEmpty() && keyRegex.matches(key)
    }

    fun validateColorHex(color: String): Boolean {
        return hexRegex.matches(color)
    }

    fun isDuplicateKey(group: String, key: String): Boolean {
        val existingStatusesList = _filteredGroups.value.flatMap { it.items }
        return existingStatusesList.any {
            it.statusGroup.equals(group, ignoreCase = true) &&
            it.statusKey.equals(key, ignoreCase = true)
        }
    }
}

