package com.example.flowdesk_android.feature.user_management.presentation.catalog

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionAction
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionCatalog
import com.example.flowdesk_android.feature.user_management.domain.model.PermissionPage
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class PermissionCatalogUiState {
    object Idle : PermissionCatalogUiState()
    object Loading : PermissionCatalogUiState()
    data class Success(val data: PermissionCatalog) : PermissionCatalogUiState()
    data class Error(val message: String) : PermissionCatalogUiState()
}

@HiltViewModel
class PermissionCatalogViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 전체 카탈로그 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val catalogFlow: Flow<Result<PermissionCatalog>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(PermissionCatalog(emptyList(), emptyList(), emptyList()))) // 로딩을 위한 초기화 발행
                val res = roleRepository.getPermissionCatalog()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태 uiState
    val uiState: StateFlow<PermissionCatalogUiState> = catalogFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            PermissionCatalogUiState.Error(result.exceptionOrNull()?.message ?: "조회 실패")
        } else {
            result.fold(
                onSuccess = { catalog ->
                    if (catalog.pages.isEmpty() && _refreshTrigger.value == 0) PermissionCatalogUiState.Loading
                    else if (catalog.pages.isEmpty()) PermissionCatalogUiState.Idle
                    else PermissionCatalogUiState.Success(catalog)
                },
                onFailure = { e ->
                    PermissionCatalogUiState.Error(e.message ?: "조회 실패")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PermissionCatalogUiState.Loading)

    // 4. 원본 캐시 리스트를 반응형으로 변환
    private val originalPages: StateFlow<List<PermissionPage>> = catalogFlow.map { result ->
        result.map { it.toPermissionPages() }.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // 6. 실시간 필터링된 페이지 및 권한 목록
    val filteredPages: StateFlow<List<PermissionPage>> = combine(originalPages, debouncedQuery) { pages, query ->
        if (query.isBlank()) {
            pages
        } else {
            pages.mapNotNull { page ->
                val matchedPermissions = page.permissions.filter { perm ->
                    perm.displayName.contains(query, ignoreCase = true) ||
                    (perm.description?.contains(query, ignoreCase = true) == true) ||
                    perm.actionDisplayName.contains(query, ignoreCase = true)
                }
                if (page.pageDisplayName.contains(query, ignoreCase = true) ||
                    page.pageName.contains(query, ignoreCase = true) ||
                    matchedPermissions.isNotEmpty()) {
                    page.copy(permissions = matchedPermissions)
                } else {
                    null
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
    }

    private fun PermissionCatalog.toPermissionPages(): List<PermissionPage> {
        return pages.map { page ->
            PermissionPage(
                pageId = page.pageId,
                pageName = page.pageName,
                pageDisplayName = page.displayName,
                permissions = permissions.filter { it.pageId == page.pageId }.map { perm ->
                    val action = actions.find { it.actionId == perm.actionId }
                    PermissionAction(
                        permissionId = perm.permissionId,
                        displayName = perm.displayName,
                        description = perm.description,
                        actionName = action?.actionName ?: "",
                        actionDisplayName = action?.displayName ?: ""
                    )
                }
            )
        }
    }
}
