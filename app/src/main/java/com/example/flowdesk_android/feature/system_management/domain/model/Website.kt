package com.example.flowdesk_android.feature.system_management.domain.model

/**
 * 웹사이트 정보 도메인 모델
 */
data class Website(
    val webCode: String,
    val userSeq: Int,
    val userName: String?,
    val webUrl: String,
    val webTitle: String,
    val webImg: String?,
    val webDesc: String?,
    val webMemo: String?,
    val isActive: Boolean,
    val duplicateAllowAfterDays: Int,
    val tenantId: Int,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * 웹사이트 목록 및 페이지네이션 결과 도메인 모델
 */
data class WebsiteListResponse(
    val items: List<Website>,
    val pageInfo: PageInfo
)
