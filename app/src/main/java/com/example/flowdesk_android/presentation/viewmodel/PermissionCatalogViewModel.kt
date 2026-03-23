package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.PermissionCatalogResponse
import com.example.flowdesk_android.domain.usecase.GetPermissionCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PermissionCatalogState {
    object Idle : PermissionCatalogState()
    object Loading : PermissionCatalogState()
    data class Success(val data: PermissionCatalogResponse) : PermissionCatalogState()
    data class Error(val message: String) : PermissionCatalogState()
}

@HiltViewModel
class PermissionCatalogViewModel @Inject constructor(
    private val getPermissionCatalogUseCase: GetPermissionCatalogUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PermissionCatalogState>(PermissionCatalogState.Idle)
    val state: StateFlow<PermissionCatalogState> = _state

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 원본 데이터 보관용 (검색에 사용)
    private var originalResponse: PermissionCatalogResponse? = null

    // 실제 화면에 보여줄 가공된 데이터
    private val _filteredPages = MutableStateFlow<List<com.example.flowdesk_android.data.remote.dto.PageDto>>(emptyList())
    val filteredPages: StateFlow<List<com.example.flowdesk_android.data.remote.dto.PageDto>> = _filteredPages

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _state.value = PermissionCatalogState.Loading
            getPermissionCatalogUseCase().fold(
                onSuccess = { response ->
                    originalResponse = response
                    _state.value = PermissionCatalogState.Success(response)
                    applyFilter("")
                },
                onFailure = { e ->
                    _state.value = PermissionCatalogState.Error(e.message ?: "카탈로그 데이터를 불러오지 못했습니다.")
                }
            )
        }
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val response = originalResponse ?: return
        
        if (query.isBlank()) {
            _filteredPages.value = response.pages.sortedBy { it.sortOrder }
        } else {
            // 검색어에 맞는 페이지(카테고리)만 필터링
            // 페이지 이름, 대칭명, 혹은 내부 매트릭스의 권한 키값 등으로 검색 가능하게 처리
            val filtered = response.pages.filter { page ->
                page.displayName.contains(query, ignoreCase = true) || 
                page.pageName.contains(query, ignoreCase = true) ||
                (response.matrix[page.pageName]?.any { it.actionName.contains(query, ignoreCase = true) } ?: false)
            }.sortedBy { it.sortOrder }
            
            _filteredPages.value = filtered
        }
    }
    
    // 이외에 matrix 데이터를 가져올 수 있는 helper 함수
    fun getActionsForPage(pageName: String) = originalResponse?.matrix?.get(pageName) ?: emptyList()
    fun getActionInfo(actionName: String) = originalResponse?.actions?.find { it.actionName == actionName }
    fun getPermissionInfo(permissionId: Int) = originalResponse?.permissions?.find { it.permissionId == permissionId }
}
