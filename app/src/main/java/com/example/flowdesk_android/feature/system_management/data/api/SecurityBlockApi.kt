package com.example.flowdesk_android.feature.system_management.data.api

import com.example.flowdesk_android.feature.system_management.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SecurityBlockApi {

    @GET("security/block-ip")
    suspend fun getBlockIps(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") q: String?
    ): Response<BlockIpListResponseDto>

    @POST("security/block-ip")
    suspend fun createBlockIp(
        @Body request: CreateBlockIpRequest
    ): Response<BlockIpItemDto>

    @GET("security/block-ip/check")
    suspend fun checkBlockIp(
        @Query("ip") ip: String?
    ): Response<IpCheckResultDto>

    @GET("security/block-ip/{id}")
    suspend fun getBlockIpDetail(
        @Path("id") id: Long
    ): Response<BlockIpItemDto>

    @PATCH("security/block-ip/{id}")
    suspend fun updateBlockIp(
        @Path("id") id: Long,
        @Body request: UpdateBlockIpRequest
    ): Response<BlockIpItemDto>

    @DELETE("security/block-ip/{id}")
    suspend fun deleteBlockIp(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("security/block-ip/bulk")
    suspend fun createBulkBlockIp(
        @Body request: BulkBlockIpRequest
    ): Response<BulkBlockResultDto>

    @GET("security/block-hp")
    suspend fun getBlockPhones(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("q") q: String?
    ): Response<BlockPhoneListResponseDto>

    @POST("security/block-hp")
    suspend fun createBlockPhone(
        @Body request: CreateBlockPhoneRequest
    ): Response<BlockPhoneItemDto>

    @GET("security/block-hp/check")
    suspend fun checkBlockPhone(
        @Query("phone") phone: String?
    ): Response<PhoneCheckResultDto>

    @GET("security/block-hp/{id}")
    suspend fun getBlockPhoneDetail(
        @Path("id") id: Long
    ): Response<BlockPhoneItemDto>

    @PATCH("security/block-hp/{id}")
    suspend fun updateBlockPhone(
        @Path("id") id: Long,
        @Body request: UpdateBlockPhoneRequest
    ): Response<BlockPhoneItemDto>

    @DELETE("security/block-hp/{id}")
    suspend fun deleteBlockPhone(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("security/block-hp/bulk")
    suspend fun createBulkBlockPhone(
        @Body request: BulkBlockPhoneRequest
    ): Response<BulkBlockPhoneResultDto>
}
