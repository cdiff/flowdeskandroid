package com.example.flowdesk_android.feature.system_management.domain.repository

import com.example.flowdesk_android.feature.system_management.domain.model.*

interface SecurityBlockRepository {
    suspend fun getBlockIps(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockIpListResponse>

    suspend fun createBlockIp(
        blockIp: String,
        reason: String,
        isActive: Int
    ): Result<BlockIpItem>

    suspend fun checkBlockIp(
        ip: String
    ): Result<IpCheckResult>

    suspend fun getBlockIpDetail(
        id: Long
    ): Result<BlockIpItem>

    suspend fun updateBlockIp(
        id: Long,
        reason: String,
        isActive: Int
    ): Result<BlockIpItem>

    suspend fun deleteBlockIp(
        id: Long
    ): Result<Unit>

    suspend fun createBulkBlockIp(
        ips: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockResult>
}
