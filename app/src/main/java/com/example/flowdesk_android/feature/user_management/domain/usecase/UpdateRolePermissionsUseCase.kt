package com.example.flowdesk_android.feature.user_management.domain.usecase

import com.example.flowdesk_android.data.local.SessionManager
import com.example.flowdesk_android.feature.auth.domain.repository.AuthRepository
import com.example.flowdesk_android.feature.user_management.domain.repository.RoleRepository
import javax.inject.Inject

class UpdateRolePermissionsUseCase @Inject constructor(
    private val roleRepository: RoleRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        roleId: Int,
        toAdd: List<Int>?,
        toRemove: List<Int>?
    ): Result<Unit> {
        return roleRepository.updateRolePermissions(roleId, toAdd, toRemove).onSuccess {
            val myInfo = sessionManager.getSession()
            val roleDetailResult = roleRepository.getRoleDetail(roleId)
            
            if (roleDetailResult.isSuccess) {
                val roleDetail = roleDetailResult.getOrNull()
                val myRoles = myInfo?.roles ?: emptyList()
                
                if (roleDetail != null && myRoles.contains(roleDetail.roleName)) {
                    authRepository.getMe().onSuccess { updatedMeInfo ->
                        sessionManager.setSession(updatedMeInfo)
                    }
                }
            }
        }
    }
}
