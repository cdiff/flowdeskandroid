package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class UpdateActionStatusUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(actionId: Int, isActive: Boolean): Result<Action> =
        repository.updateActionStatus(actionId, isActive)
}
