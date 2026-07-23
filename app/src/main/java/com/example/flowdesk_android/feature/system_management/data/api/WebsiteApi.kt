package com.example.flowdesk_android.feature.system_management.data.api

import com.example.flowdesk_android.feature.system_management.data.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 웹사이트 관리 API 명세 인터페이스
 */
interface WebsiteApi {

    @GET("websites")
    suspend fun getWebsites(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") query: String?
    ): Response<WebsiteListResponseDto>

    @POST("websites")
    suspend fun createWebsite(
        @Body request: CreateWebsiteRequestDto
    ): Response<WebsiteItemDto>

    @GET("websites/{webCode}")
    suspend fun getWebsiteDetail(
        @Path("webCode") webCode: String
    ): Response<WebsiteItemDto>

    @PATCH("websites/{webCode}")
    suspend fun updateWebsite(
        @Path("webCode") webCode: String,
        @Body request: UpdateWebsiteRequestDto
    ): Response<WebsiteItemDto>

    @DELETE("websites/{webCode}")
    suspend fun deleteWebsite(
        @Path("webCode") webCode: String
    ): Response<Unit>

    @PATCH("websites/{webCode}/status")
    suspend fun updateWebsiteStatus(
        @Path("webCode") webCode: String,
        @Body request: UpdateWebsiteStatusRequestDto
    ): Response<WebsiteItemDto>
}
