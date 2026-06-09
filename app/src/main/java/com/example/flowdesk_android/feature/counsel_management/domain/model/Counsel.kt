package com.example.flowdesk_android.feature.counsel_management.domain.model

data class CounselList(
    val items: List<CounselItem>,
    val pageInfo: PageInfo?
)

data class CounselItem(
    val counselSeq: Int,
    val webCode: String,
    val webTitle: String,
    val name: String,
    val counselHp: String,
    val counselStat: Int,
    val statusName: String,
    val empSeq: Int?,
    val empName: String?,
    val duplicateState: String,
    val counselResvDtm: String?,
    val regDtm: String,
    val editDtm: String,
    val fieldValues: List<CounselFieldValue>
)

data class CounselFieldValue(
    val fieldId: Int,
    val fieldKey: String,
    val label: String,
    val fieldType: String,
    val valueText: String?,
    val valueNumber: Double?,
    val valueDate: String?,
    val valueDatetime: String?
)

data class PageInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)
