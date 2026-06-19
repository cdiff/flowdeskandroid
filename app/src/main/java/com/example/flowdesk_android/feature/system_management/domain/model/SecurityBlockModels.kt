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
