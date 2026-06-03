package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class UpdateActionUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        actionId: Int,
        actionName: String? = null,
        displayName: String? = null,
        isActive: Int? = null
    ): Result<Action> = repository.updateAction(actionId, actionName, displayName, isActive)
}
