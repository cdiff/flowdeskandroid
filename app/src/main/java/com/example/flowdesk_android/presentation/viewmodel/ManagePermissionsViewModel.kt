package com.example.flowdesk_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowdesk_android.data.remote.dto.PermissionPageDto
import com.example.flowdesk_android.data.remote.dto.RoleDetailResponse
import com.example.flowdesk_android.data.remote.dto.UpdateRoleInfoRequest
import com.example.flowdesk_android.data.remote.dto.UpdateRolePermissionsRequest
import com.example.flowdesk_android.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ManagePermissionsState {
    object Idle : ManagePermissionsState()
    object Loading : ManagePermissionsState()
    data class Loaded(val role: RoleDetailResponse) : ManagePermissionsState()
    object InfoUpdateSuccess : ManagePermissionsState()
    object PermissionsUpdateSuccess : ManagePermissionsState()
    data class Error(val message: String) : ManagePermissionsState()
}

@HiltViewModel
class ManagePermissionsViewModel @Inject constructor(
    private val getRoleDetailUseCase: GetRoleDetailUseCase,
    private val getPermissionCatalogUseCase: GetPermissionCatalogUseCase,
    private val updateRoleInfoUseCase: UpdateRoleInfoUseCase,
    private val updateRolePermissionsUseCase: UpdateRolePermissionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ManagePermissionsState>(ManagePermissionsState.Idle)
    val state: StateFlow<ManagePermissionsState> = _state

    // 현재 로드된 역할의 권한 페이지 목록 (세부권한탭에서 사용)
    private val _permissionPages = MutableStateFlow<List<PermissionPageDto>>(emptyList())
    val permissionPages: StateFlow<List<PermissionPageDto>> = _permissionPages

    // 현재 체크된 permissionId 집합
    val checkedPermissionIds = mutableSetOf<Int>()

    fun loadRoleDetail(roleId: Int) {
        viewModelScope.launch {
            _state.value = ManagePermissionsState.Loading
            
            // 1. Load Permission Catalog first
            val catalogResult = getPermissionCatalogUseCase()
            if (catalogResult.isFailure) {
                _state.value = ManagePermissionsState.Error("권한 카탈로그를 불러오지 못했습니다.")
                return@launch
            }
            val catalog = catalogResult.getOrThrow()

            // 2. Load Role Detail to get current permissions
            getRoleDetailUseCase(roleId).fold(
                onSuccess = { role ->
                    // Clear and initialize checked permissions from role detail
                    checkedPermissionIds.clear()
                    role.permissionsByPage?.forEach { page ->
                        page.permissions?.forEach { action ->
                            checkedPermissionIds.add(action.permissionId)
                        }
                    }

                    // 3. Transform flattened catalog into nested UI structure (PermissionPageDto)
                    // We only show entries that exist in the matrix (actual permission mappings)
                    val transformedPages = catalog.pages.mapNotNull { pageDto ->
                        val matrixForPage = catalog.matrix[pageDto.pageName] ?: return@mapNotNull null
                        
                        val actionDtos = matrixForPage.mapNotNull { matrixItem ->
                            val actionInfo = catalog.actions.find { it.actionName == matrixItem.actionName } 
                            com.example.flowdesk_android.data.remote.dto.PermissionActionDto(
                                permissionId = matrixItem.permissionId,
                                displayName = actionInfo?.displayName ?: matrixItem.actionName,
                                description = null,
                                actionId = actionInfo?.actionId ?: 0,
                                actionName = matrixItem.actionName,
                                actionDisplayName = actionInfo?.displayName ?: matrixItem.actionName
                            )
                        }

                        if (actionDtos.isEmpty()) return@mapNotNull null

                        com.example.flowdesk_android.data.remote.dto.PermissionPageDto(
                            pageId = pageDto.pageId,
                            pageName = pageDto.pageName,
                            pageDisplayName = pageDto.displayName,
                            permissions = actionDtos
                        )
                    }.sortedByDescending { it.pageId } // Sorting logic if needed, or by sortOrder

                    _permissionPages.value = transformedPages
                    _state.value = ManagePermissionsState.Loaded(role)
                },
                onFailure = { e ->
                    _state.value = ManagePermissionsState.Error(e.message ?: "역할 정보를 불러오지 못했습니다.")
                }
            )
        }
    }

    fun updateRoleInfo(roleId: Int, roleName: String, displayName: String, description: String?) {
        if (roleName.isBlank() || displayName.isBlank()) {
            _state.value = ManagePermissionsState.Error("역할 이름과 표시명은 필수 항목입니다.")
            return
        }
        viewModelScope.launch {
            _state.value = ManagePermissionsState.Loading
            val request = UpdateRoleInfoRequest(roleName, displayName, description)
            updateRoleInfoUseCase(roleId, request).fold(
                onSuccess = { _state.value = ManagePermissionsState.InfoUpdateSuccess },
                onFailure = { e -> _state.value = ManagePermissionsState.Error(e.message ?: "정보 수정 실패") }
            )
        }
    }

    fun savePermissions(roleId: Int, originalIds: Set<Int>) {
        val current = checkedPermissionIds.toSet()
        val toAdd = (current - originalIds).toList().ifEmpty { null }
        val toRemove = (originalIds - current).toList().ifEmpty { null }

        if (toAdd == null && toRemove == null) {
            _state.value = ManagePermissionsState.Error("변경된 권한이 없습니다.")
            return
        }
        viewModelScope.launch {
            _state.value = ManagePermissionsState.Loading
            val request = UpdateRolePermissionsRequest(add = toAdd, remove = toRemove)
            updateRolePermissionsUseCase(roleId, request).fold(
                onSuccess = { _state.value = ManagePermissionsState.PermissionsUpdateSuccess },
                onFailure = { e -> _state.value = ManagePermissionsState.Error(e.message ?: "권한 저장 실패") }
            )
        }
    }

    fun resetState() {
        _state.value = ManagePermissionsState.Idle
    }
}
