package com.example.flowdesk_android.feature.system_management.domain.repository

import com.example.flowdesk_android.feature.system_management.domain.model.Website
import com.example.flowdesk_android.feature.system_management.domain.model.WebsiteListResponse

/**
 * 웹사이트 관리를 위한 도메인 레포지토리 인터페이스
 */
interface WebsiteRepository {
    suspend fun getWebsites(
        page: Int,
        limit: Int,
        query: String?
    ): Result<WebsiteListResponse>

    suspend fun createWebsite(
        webCode: String,
        userSeq: Int,
        webUrl: String,
        webTitle: String,
        webImg: String?,
        webDesc: String?,
        webMemo: String?,
        isActive: Boolean,
        duplicateAllowAfterDays: Int
    ): Result<Website>

    suspend fun getWebsiteDetail(
        webCode: String
    ): Result<Website>

    suspend fun updateWebsite(
        webCode: String,
        userSeq: Int,
        webUrl: String,
        webTitle: String,
        webImg: String?,
        webDesc: String?,
        webMemo: String?,
        isActive: Boolean,
        duplicateAllowAfterDays: Int
    ): Result<Website>

    suspend fun deleteWebsite(
        webCode: String
    ): Result<Unit>

    suspend fun updateWebsiteStatus(
        webCode: String,
        isActive: Boolean
    ): Result<Website>
}
