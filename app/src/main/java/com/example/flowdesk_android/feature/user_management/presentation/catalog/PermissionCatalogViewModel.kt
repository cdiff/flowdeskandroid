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
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<PermissionCatalogUiState>(PermissionCatalogUiState.Idle)
    val uiState: StateFlow<PermissionCatalogUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var originalPages: List<PermissionPage> = emptyList()

    private val _filteredPages = MutableStateFlow<List<PermissionPage>>(emptyList())
    val filteredPages: StateFlow<List<PermissionPage>> = _filteredPages.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = PermissionCatalogUiState.Loading
            roleRepository.getPermissionCatalog()
                .onSuccess { catalog ->
                    originalPages = catalog.toPermissionPages()
                    _uiState.value = PermissionCatalogUiState.Success(catalog)
                    applyFilter("")
                }
                .onFailure { e ->
                    _uiState.value = PermissionCatalogUiState.Error(e.message ?: "카탈로그 데이터를 불러오지 못했습니다.")
                }
        }
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        if (query.isBlank()) {
            _filteredPages.value = originalPages
        } else {
            _filteredPages.value = originalPages.mapNotNull { page ->
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
