package com.example.flowdesk_android.feature.system_management.data.repository

import com.example.flowdesk_android.feature.system_management.data.api.SecurityBlockApi
import com.example.flowdesk_android.feature.system_management.data.dto.*
import com.example.flowdesk_android.feature.system_management.domain.model.*
import com.example.flowdesk_android.feature.system_management.domain.repository.SecurityBlockRepository
import javax.inject.Inject

class SecurityBlockRepositoryImpl @Inject constructor(
    private val apiService: SecurityBlockApi
) : SecurityBlockRepository {

    override suspend fun getBlockIps(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockIpListResponse> = runCatching {
        val response = apiService.getBlockIps(page, limit, q)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBlockIp(
        blockIp: String,
        reason: String,
        isActive: Int
    ): Result<BlockIpItem> = runCatching {
        val request = CreateBlockIpRequest(blockIp = blockIp, reason = reason, isActive = isActive)
        val response = apiService.createBlockIp(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun checkBlockIp(
        ip: String
    ): Result<IpCheckResult> = runCatching {
        val response = apiService.checkBlockIp(ip)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getBlockIpDetail(
        id: Long
    ): Result<BlockIpItem> = runCatching {
        val response = apiService.getBlockIpDetail(id)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateBlockIp(
        id: Long,
        reason: String,
        isActive: Int
    ): Result<BlockIpItem> = runCatching {
        val request = UpdateBlockIpRequest(reason = reason, isActive = isActive)
        val response = apiService.updateBlockIp(id, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun deleteBlockIp(
        id: Long
    ): Result<Unit> = runCatching {
        val response = apiService.deleteBlockIp(id)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBulkBlockIp(
        ips: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockResult> = runCatching {
        val request = BulkBlockIpRequest(ips = ips, reason = reason, isActive = isActive)
        val response = apiService.createBulkBlockIp(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }
}
