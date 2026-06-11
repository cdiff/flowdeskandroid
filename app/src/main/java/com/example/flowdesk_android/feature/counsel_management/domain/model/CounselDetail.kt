package com.example.flowdesk_android.feature.counsel_management.domain.model

data class CounselDetail(
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
    val counselIp: String?,
    val counselSource: String?,
    val counselMedium: String?,
    val counselCampaign: String?,
    val counselMemo: String?,
    val fieldValues: List<CounselFieldValue>,
    val logs: List<CounselLog>,
    val memos: List<CounselMemo>
)

data class CounselLog(
    val counselSeq: Int,
    val logNo: Int,
    val counselStat: Int,
    val statusName: String,
    val regDtm: String
)

data class CounselMemo(
    val memoLogId: Int,
    val counselSeq: Int,
    val statusId: Int,
    val statusName: String,
    val memoText: String,
    val creatorName: String,
    val createdAt: String
)
