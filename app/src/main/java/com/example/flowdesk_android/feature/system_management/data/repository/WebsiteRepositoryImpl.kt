package com.example.flowdesk_android.feature.system_management.data.repository

import com.example.flowdesk_android.feature.system_management.data.api.WebsiteApi
import com.example.flowdesk_android.feature.system_management.data.dto.*
import com.example.flowdesk_android.feature.system_management.domain.model.Website
import com.example.flowdesk_android.feature.system_management.domain.model.WebsiteListResponse
import com.example.flowdesk_android.feature.system_management.domain.repository.WebsiteRepository
import javax.inject.Inject

/**
 * 웹사이트 레포지토리 구현체
 */
class WebsiteRepositoryImpl @Inject constructor(
    private val apiService: WebsiteApi
) : WebsiteRepository {

    override suspend fun getWebsites(
        page: Int,
        limit: Int,
        query: String?
    ): Result<WebsiteListResponse> = runCatching {
        val response = apiService.getWebsites(page, limit, query)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createWebsite(
        webCode: String,
        userSeq: Int,
        webUrl: String,
        webTitle: String,
        webImg: String?,
        webDesc: String?,
        webMemo: String?,
        isActive: Boolean,
        duplicateAllowAfterDays: Int
    ): Result<Website> = runCatching {
        val request = CreateWebsiteRequestDto(
            webCode = webCode,
            userSeq = userSeq,
            webUrl = webUrl,
            webTitle = webTitle,
            webImg = webImg,
            webDesc = webDesc,
            webMemo = webMemo,
            isActive = if (isActive) 1 else 0,
            duplicateAllowAfterDays = duplicateAllowAfterDays
        )
        val response = apiService.createWebsite(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getWebsiteDetail(
        webCode: String
    ): Result<Website> = runCatching {
        val response = apiService.getWebsiteDetail(webCode)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateWebsite(
        webCode: String,
        userSeq: Int,
        webUrl: String,
        webTitle: String,
        webImg: String?,
        webDesc: String?,
        webMemo: String?,
        isActive: Boolean,
        duplicateAllowAfterDays: Int
    ): Result<Website> = runCatching {
        val request = UpdateWebsiteRequestDto(
            userSeq = userSeq,
            webUrl = webUrl,
            webTitle = webTitle,
            webImg = webImg,
            webDesc = webDesc,
            webMemo = webMemo,
            isActive = if (isActive) 1 else 0,
            duplicateAllowAfterDays = duplicateAllowAfterDays
        )
        val response = apiService.updateWebsite(webCode, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun deleteWebsite(
        webCode: String
    ): Result<Unit> = runCatching {
        val response = apiService.deleteWebsite(webCode)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateWebsiteStatus(
        webCode: String,
        isActive: Boolean
    ): Result<Website> = runCatching {
        val request = UpdateWebsiteStatusRequestDto(
            isActive = if (isActive) 1 else 0
        )
        val response = apiService.updateWebsiteStatus(webCode, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }
}
