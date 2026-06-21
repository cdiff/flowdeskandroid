package com.example.flowdesk_android.feature.system_management.data.dto

import com.example.flowdesk_android.feature.system_management.domain.model.*
import com.google.gson.annotations.SerializedName

data class BlockIpItemDto(
    @SerializedName("dbiIdx") val dbiIdx: Long,
    @SerializedName("tenantId") val tenantId: Long,
    @SerializedName("blockIp") val blockIp: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("createdBy") val createdBy: Long,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(): BlockIpItem = BlockIpItem(
        dbiIdx = dbiIdx,
        tenantId = tenantId,
        blockIp = blockIp,
        reason = reason,
        isActive = isActive == 1,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class PageInfoDto(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
) {
    fun toDomain(): PageInfo = PageInfo(
        page = page,
        limit = limit,
        totalItems = totalItems,
        totalPages = totalPages
    )
}

data class BlockIpListResponseDto(
    @SerializedName("items") val items: List<BlockIpItemDto>,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto
) {
    fun toDomain(): BlockIpListResponse = BlockIpListResponse(
        items = items.map { it.toDomain() },
        pageInfo = pageInfo.toDomain()
    )
}

data class IpCheckResultDto(
    @SerializedName("isBlocked") val isBlocked: Boolean,
    @SerializedName("reason") val reason: String?,
    @SerializedName("blockId") val blockId: Long?,
    @SerializedName("matchedWord") val matchedWord: String?
) {
    fun toDomain(): IpCheckResult = IpCheckResult(
        isBlocked = isBlocked,
        reason = reason,
        blockId = blockId,
        matchedWord = matchedWord
    )
}

data class BulkBlockResultDto(
    @SerializedName("successCount") val successCount: Int,
    @SerializedName("skippedCount") val skippedCount: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("skippedIps") val skippedIps: List<String>?
) {
    fun toDomain(): BulkBlockResult = BulkBlockResult(
        successCount = successCount,
        skippedCount = skippedCount,
        totalCount = totalCount,
        skippedIps = skippedIps ?: emptyList()
    )
}

data class CreateBlockIpRequest(
    @SerializedName("blockIp") val blockIp: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)

data class UpdateBlockIpRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)

data class BulkBlockIpRequest(
    @SerializedName("ips") val ips: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)

data class BlockPhoneItemDto(
    @SerializedName("dbhIdx") val dbhIdx: Long,
    @SerializedName("tenantId") val tenantId: Long,
    @SerializedName("blockHp") val blockHp: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("createdBy") val createdBy: Long,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toDomain(): BlockPhoneItem = BlockPhoneItem(
        dbhIdx = dbhIdx,
        tenantId = tenantId,
        blockHp = blockHp,
        reason = reason,
        isActive = isActive == 1,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class BlockPhoneListResponseDto(
    @SerializedName("items") val items: List<BlockPhoneItemDto>,
    @SerializedName("pageInfo") val pageInfo: PageInfoDto
) {
    fun toDomain(): BlockPhoneListResponse = BlockPhoneListResponse(
        items = items.map { it.toDomain() },
        pageInfo = pageInfo.toDomain()
    )
}

data class PhoneCheckResultDto(
    @SerializedName("isBlocked") val isBlocked: Boolean,
    @SerializedName("reason") val reason: String?,
    @SerializedName("blockId") val blockId: Long?,
    @SerializedName("matchedWord") val matchedWord: String?
) {
    fun toDomain(): PhoneCheckResult = PhoneCheckResult(
        isBlocked = isBlocked,
        reason = reason,
        blockId = blockId,
        matchedWord = matchedWord
    )
}

data class BulkBlockPhoneResultDto(
    @SerializedName("successCount") val successCount: Int,
    @SerializedName("skippedCount") val skippedCount: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("skippedPhones") val skippedPhones: List<String>?
) {
    fun toDomain(): BulkBlockPhoneResult = BulkBlockPhoneResult(
        successCount = successCount,
        skippedCount = skippedCount,
        totalCount = totalCount,
        skippedPhones = skippedPhones ?: emptyList()
    )
}

data class CreateBlockPhoneRequest(
    @SerializedName("blockHp") val blockHp: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)

data class UpdateBlockPhoneRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)

data class BulkBlockPhoneRequest(
    @SerializedName("phones") val phones: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("isActive") val isActive: Int
)
