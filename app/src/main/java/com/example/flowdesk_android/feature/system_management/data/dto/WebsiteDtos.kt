package com.example.flowdesk_android.feature.system_management.data.dto

import com.example.flowdesk_android.feature.system_management.domain.model.Website
import com.example.flowdesk_android.feature.system_management.domain.model.WebsiteListResponse
import com.google.gson.annotations.SerializedName

/**
 * 웹사이트 항목 API 응답 DTO
 */
data class WebsiteItemDto(
    @SerializedName("webCode") val webCode: String,
    @SerializedName("userSeq") val userSeq: Int,
    @SerializedName("userName") val userName: String?,
    @SerializedName("webUrl") val webUrl: String,
    @SerializedName("webTitle") val webTitle: String,
    @SerializedName("webImg") val webImg: String?,
    @SerializedName("webDesc") val webDesc: String?,
    @SerializedName("webMemo") val webMemo: String?,
    @SerializedName("isActive") val isActive: Int, // 1: 활성, 0: 비활성
    @SerializedName("duplicateAllowAfterDays") val duplicateAllowAfterDays: Int,
    @SerializedName("tenantId") val tenantId: Int,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(): Website = Website(
        webCode = webCode,
        userSeq = userSeq,
        userName = userName,
        webUrl = webUrl,
        webTitle = webTitle,
        webImg = webImg,
        webDesc = webDesc,
        webMemo = webMemo,
        isActive = isActive == 1,
        duplicateAllowAfterDays = duplicateAllowAfterDays,
        tenantId = tenantId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * 웹사이트 목록 조회 응답 DTO
 */
data class WebsiteListResponseDto(
    @SerializedName("items") val items: List<WebsiteItemDto>,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto
) {
    fun toDomain(): WebsiteListResponse = WebsiteListResponse(
        items = items.map { it.toDomain() },
        pageInfo = pageInfo.toDomain()
    )
}

/**
 * 웹사이트 등록 요청 바디 DTO
 */
data class CreateWebsiteRequestDto(
    @SerializedName("webCode") val webCode: String,
    @SerializedName("userSeq") val userSeq: Int,
    @SerializedName("webUrl") val webUrl: String,
    @SerializedName("webTitle") val webTitle: String,
    @SerializedName("webImg") val webImg: String?,
    @SerializedName("webDesc") val webDesc: String?,
    @SerializedName("webMemo") val webMemo: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("duplicateAllowAfterDays") val duplicateAllowAfterDays: Int
)

/**
 * 웹사이트 정보 수정 요청 바디 DTO
 */
data class UpdateWebsiteRequestDto(
    @SerializedName("userSeq") val userSeq: Int,
    @SerializedName("webUrl") val webUrl: String,
    @SerializedName("webTitle") val webTitle: String,
    @SerializedName("webImg") val webImg: String?,
    @SerializedName("webDesc") val webDesc: String?,
    @SerializedName("webMemo") val webMemo: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("duplicateAllowAfterDays") val duplicateAllowAfterDays: Int
)

/**
 * 웹사이트 상태 수정 요청 바디 DTO
 */
data class UpdateWebsiteStatusRequestDto(
    @SerializedName("isActive") val isActive: Int
)
