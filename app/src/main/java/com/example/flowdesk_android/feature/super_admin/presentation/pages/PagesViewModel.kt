package com.example.flowdesk_android.feature.super_admin.presentation.pages

import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.core.base.BaseViewModel
import com.example.flowdesk_android.feature.super_admin.domain.model.Page
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────
sealed class PageListUiState {
    object Loading : PageListUiState()
    object Empty : PageListUiState()
    data class Success(val pages: List<Page>) : PageListUiState()
    data class Error(val message: String) : PageListUiState()
}

// ── One-shot Events ───────────────────────────────────────
sealed class PageListEvent {
    object PageCreated : PageListEvent()
    object PageUpdated : PageListEvent()
    object PageDeleted : PageListEvent()
    data class Error(val message: String) : PageListEvent()
}

@HiltViewModel
class PagesViewModel @Inject constructor(
    private val superRepository: SuperRepository
) : BaseViewModel() {

    // 1. 수동 리프레시 트리거
    private val _refreshTrigger = MutableStateFlow(0)

    // 2. 전체 페이지 가져오는 흐름
    @OptIn(ExperimentalCoroutinesApi::class)
    private val pagesFlow: Flow<Result<List<Page>>> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(Result.success(emptyList())) // 로딩 상태 전이를 위해 발행
                val res = superRepository.getPages()
                emit(res)
            }
        }

    // 3. UI 로딩/에러/성공 상태 uiState
    val uiState: StateFlow<PageListUiState> = pagesFlow.map { result ->
        if (_refreshTrigger.value > 0 && result.getOrNull() == null) {
            PageListUiState.Error(result.exceptionOrNull()?.message ?: "조회 실패")
        } else {
            result.fold(
                onSuccess = { pages ->
                    if (pages.isEmpty() && _refreshTrigger.value == 0) PageListUiState.Loading
                    else if (pages.isEmpty()) PageListUiState.Empty
                    else PageListUiState.Success(pages)
                },
                onFailure = { e ->
                    PageListUiState.Error(e.message ?: "조회 실패")
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PageListUiState.Loading)

    // 4. 전체 페이지 캐시 StateFlow
    private val allPages: StateFlow<List<Page>> = pagesFlow.map { result ->
        result.getOrDefault(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 5. 검색 쿼리 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
    
    private val _expandedParents = MutableStateFlow<Set<Int>>(emptySet())
    val expandedParents: StateFlow<Set<Int>> = _expandedParents.asStateFlow()

    // 6. 실시간 필터링된 페이지 목록
    val filteredPages: StateFlow<List<Page>> = combine(allPages, debouncedQuery, _expandedParents) { pages, query, expanded ->
        if (query.isNotBlank()) {
            pages.filter {
                it.pageName.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true) ||
                it.path.contains(query, ignoreCase = true)
            }
        } else {
            val result = mutableListOf<Page>()
            val roots = pages.filter { it.parentId == null }.sortedBy { it.sortOrder }
            val childrenMap = pages.filter { it.parentId != null }.groupBy { it.parentId }

            for (root in roots) {
                result.add(root)
                if (expanded.contains(root.pageId)) {
                    childrenMap[root.pageId]?.sortedBy { it.sortOrder }?.let { children ->
                        result.addAll(children)
                    }
                }
            }
            result
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = Channel<PageListEvent>()
    val event: Flow<PageListEvent> = _event.receiveAsFlow()

    init {
        triggerRefresh()
    }

    fun triggerRefresh() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun toggleParent(pageId: Int) {
        val current = _expandedParents.value.toMutableSet()
        if (current.contains(pageId)) {
            current.remove(pageId)
        } else {
            current.add(pageId)
        }
        _expandedParents.value = current
    }

    fun createPage(
        pageName: String,
        path: String,
        displayName: String,
        description: String?,
        parentId: Int?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            superRepository.createPage(pageName, path, displayName, description, parentId, sortOrder)
                .onSuccess {
                    _event.send(PageListEvent.PageCreated)
                    triggerRefresh()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "생성 실패")) }
        }
    }

    fun updatePageStatus(page: Page, isActive: Boolean) {
        viewModelScope.launch {
            superRepository.updatePage(
                pageId = page.pageId,
                pageName = null,
                path = null,
                displayName = null,
                description = null,
                parentId = null,
                sortOrder = null,
                isActive = if (isActive) 1 else 0
            )
                .onSuccess {
                    _event.send(PageListEvent.PageUpdated)
                    triggerRefresh()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "상태 변경 실패")) }
        }
    }

    fun deletePage(pageId: Int) {
        viewModelScope.launch {
            superRepository.deletePage(pageId)
                .onSuccess {
                    _event.send(PageListEvent.PageDeleted)
                    triggerRefresh()
                }
                .onFailure { _event.send(PageListEvent.Error(it.message ?: "삭제 실패")) }
        }
    }
}
