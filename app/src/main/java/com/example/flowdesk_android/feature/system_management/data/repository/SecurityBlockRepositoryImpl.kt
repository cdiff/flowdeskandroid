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

    override suspend fun getBlockPhones(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockPhoneListResponse> = runCatching {
        val response = apiService.getBlockPhones(page, limit, q)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBlockPhone(
        blockHp: String,
        reason: String,
        isActive: Int
    ): Result<BlockPhoneItem> = runCatching {
        val request = CreateBlockPhoneRequest(blockHp = blockHp, reason = reason, isActive = isActive)
        val response = apiService.createBlockPhone(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun checkBlockPhone(
        phone: String
    ): Result<PhoneCheckResult> = runCatching {
        val response = apiService.checkBlockPhone(phone)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getBlockPhoneDetail(
        id: Long
    ): Result<BlockPhoneItem> = runCatching {
        val response = apiService.getBlockPhoneDetail(id)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateBlockPhone(
        id: Long,
        reason: String,
        isActive: Int
    ): Result<BlockPhoneItem> = runCatching {
        val request = UpdateBlockPhoneRequest(reason = reason, isActive = isActive)
        val response = apiService.updateBlockPhone(id, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun deleteBlockPhone(
        id: Long
    ): Result<Unit> = runCatching {
        val response = apiService.deleteBlockPhone(id)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBulkBlockPhone(
        phones: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockPhoneResult> = runCatching {
        val request = BulkBlockPhoneRequest(phones = phones, reason = reason, isActive = isActive)
        val response = apiService.createBulkBlockPhone(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getBlockWords(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockWordListResponse> = runCatching {
        val response = apiService.getBlockWords(page, limit, q)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBlockWord(
        blockWord: String,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BlockWordItem> = runCatching {
        val request = CreateBlockWordRequest(blockWord = blockWord, matchType = matchType, reason = reason, isActive = isActive)
        val response = apiService.createBlockWord(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun checkBlockWord(
        word: String
    ): Result<WordCheckResult> = runCatching {
        val response = apiService.checkBlockWord(word)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun getBlockWordDetail(
        id: Long
    ): Result<BlockWordItem> = runCatching {
        val response = apiService.getBlockWordDetail(id)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun updateBlockWord(
        id: Long,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BlockWordItem> = runCatching {
        val request = UpdateBlockWordRequest(matchType = matchType, reason = reason, isActive = isActive)
        val response = apiService.updateBlockWord(id, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun deleteBlockWord(
        id: Long
    ): Result<Unit> = runCatching {
        val response = apiService.deleteBlockWord(id)
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code()}")
        }
    }

    override suspend fun createBulkBlockWord(
        words: String,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockWordResult> = runCatching {
        val request = BulkBlockWordRequest(words = words, matchType = matchType, reason = reason, isActive = isActive)
        val response = apiService.createBulkBlockWord(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("API error: ${response.code()}")
        }
    }
}
