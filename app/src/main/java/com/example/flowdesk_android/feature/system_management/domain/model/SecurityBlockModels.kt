package com.example.flowdesk_android.feature.system_management.domain.model

data class BlockIpItem(
    val dbiIdx: Long,
    val tenantId: Long,
    val blockIp: String,
    val reason: String?,
    val isActive: Boolean,
    val createdBy: Long,
    val createdAt: String?,
    val updatedAt: String?
)

data class PageInfo(
    val page: Int,
    val limit: Int,
    val totalItems: Int,
    val totalPages: Int
)

data class BlockIpListResponse(
    val items: List<BlockIpItem>,
    val pageInfo: PageInfo
)

data class IpCheckResult(
    val isBlocked: Boolean,
    val reason: String?,
    val blockId: Long?,
    val matchedWord: String?
)

data class BulkBlockResult(
    val successCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val skippedIps: List<String>
)

data class BlockPhoneItem(
    val dbhIdx: Long,
    val tenantId: Long,
    val blockHp: String,
    val reason: String?,
    val isActive: Boolean,
    val createdBy: Long,
    val createdAt: String?,
    val updatedAt: String?
)

data class BlockPhoneListResponse(
    val items: List<BlockPhoneItem>,
    val pageInfo: PageInfo
)

data class PhoneCheckResult(
    val isBlocked: Boolean,
    val reason: String?,
    val blockId: Long?,
    val matchedWord: String?
)

data class BulkBlockPhoneResult(
    val successCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val skippedPhones: List<String>
)

data class BlockWordItem(
    val dbwIdx: Long,
    val tenantId: Long,
    val blockWord: String,
    val matchType: String,
    val reason: String?,
    val isActive: Boolean,
    val createdBy: Long,
    val createdAt: String?,
    val updatedAt: String?
)

data class BlockWordListResponse(
    val items: List<BlockWordItem>,
    val pageInfo: PageInfo
)

data class WordCheckResult(
    val isBlocked: Boolean,
    val reason: String?,
    val blockId: Long?,
    val matchedWord: String?
)

data class BulkBlockWordResult(
    val successCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val skippedWords: List<String>
)
