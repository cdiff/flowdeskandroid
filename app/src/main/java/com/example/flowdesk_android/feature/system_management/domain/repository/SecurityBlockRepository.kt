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

    suspend fun getBlockPhones(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockPhoneListResponse>

    suspend fun createBlockPhone(
        blockHp: String,
        reason: String,
        isActive: Int
    ): Result<BlockPhoneItem>

    suspend fun checkBlockPhone(
        phone: String
    ): Result<PhoneCheckResult>

    suspend fun getBlockPhoneDetail(
        id: Long
    ): Result<BlockPhoneItem>

    suspend fun updateBlockPhone(
        id: Long,
        reason: String,
        isActive: Int
    ): Result<BlockPhoneItem>

    suspend fun deleteBlockPhone(
        id: Long
    ): Result<Unit>

    suspend fun createBulkBlockPhone(
        phones: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockPhoneResult>

    suspend fun getBlockWords(
        page: Int,
        limit: Int,
        q: String?
    ): Result<BlockWordListResponse>

    suspend fun createBlockWord(
        blockWord: String,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BlockWordItem>

    suspend fun checkBlockWord(
        word: String
    ): Result<WordCheckResult>

    suspend fun getBlockWordDetail(
        id: Long
    ): Result<BlockWordItem>

    suspend fun updateBlockWord(
        id: Long,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BlockWordItem>

    suspend fun deleteBlockWord(
        id: Long
    ): Result<Unit>

    suspend fun createBulkBlockWord(
        words: String,
        matchType: String,
        reason: String,
        isActive: Int
    ): Result<BulkBlockWordResult>
}
