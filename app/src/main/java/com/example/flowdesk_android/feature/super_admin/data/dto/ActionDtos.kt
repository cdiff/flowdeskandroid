package com.example.flowdesk_android.feature.super_admin.data.dto

import com.example.flowdesk_android.feature.super_admin.domain.model.Action
import com.google.gson.annotations.SerializedName

// ──────────────────────────────────────────────────
// Request DTOs
// ──────────────────────────────────────────────────

data class CreateActionRequest(
    @SerializedName("actionName")   val actionName: String,
    @SerializedName("displayName")  val displayName: String,
    @SerializedName("isActive")     val isActive: Int = 1
)

data class UpdateActionRequest(
    @SerializedName("actionName")   val actionName: String? = null,
    @SerializedName("displayName")  val displayName: String? = null,
    @SerializedName("isActive")     val isActive: Int? = null
)

data class UpdateActionStatusRequest(
    @SerializedName("isActive") val isActive: Int
)

// ──────────────────────────────────────────────────
// Response DTOs
// ──────────────────────────────────────────────────

data class ActionDto(
    @SerializedName("actionId")        val actionId: Int = 0,
    @SerializedName("actionName")      val actionName: String = "",
    @SerializedName("displayName")     val displayName: String = "",
    @SerializedName("isActive")        val isActive: Int = 1,
    @SerializedName("permissionCount") val permissionCount: Int = 0,
    @SerializedName("createdAt")       val createdAt: String? = null,
    @SerializedName("updatedAt")       val updatedAt: String? = null
) {
    fun toDomain() = Action(
        actionId        = actionId,
        actionName      = actionName,
        displayName     = displayName,
        isActive        = isActive == 1,
        permissionCount = permissionCount,
        createdAt       = createdAt,
        updatedAt       = updatedAt
    )
}

data class ActionsResponse(
    @SerializedName("items")    val items: List<ActionDto>?,
    @SerializedName("pageInfo") val pageInfo: ActionPageInfoDto?
)

data class ActionPageInfoDto(
    @SerializedName("page")       val page: Int,
    @SerializedName("limit")      val limit: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
)
