package com.example.flowdesk_android.domain.usecase

import com.example.flowdesk_android.data.remote.dto.PermissionCatalogResponse
import com.example.flowdesk_android.domain.repository.RoleRepository
import javax.inject.Inject

class GetPermissionCatalogUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(): Result<PermissionCatalogResponse> {
        return repository.getPermissionCatalog()
    }
}
