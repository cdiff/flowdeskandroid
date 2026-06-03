package com.example.flowdesk_android.feature.super_admin.domain.usecase

import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.example.flowdesk_android.feature.super_admin.domain.repository.SuperRepository
import javax.inject.Inject

class CreateActionUseCase @Inject constructor(
    private val repository: SuperRepository
) {
    suspend operator fun invoke(
        actionName: String,
        displayName: String
    ): Result<Action> = repository.createAction(actionName, displayName)
}
