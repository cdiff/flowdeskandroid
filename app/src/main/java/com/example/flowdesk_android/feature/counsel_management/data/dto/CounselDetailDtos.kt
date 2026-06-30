package com.example.flowdesk_android.feature.counsel_management.data.dto

import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselDetail
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselFieldValue
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselLog
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselMemo
import com.google.gson.annotations.SerializedName

// ── 단건 조회 / 수정 응답 DTO ──────────────────────────────────────────────────

data class CounselDetailResponse(
    @SerializedName("counselSeq")     val counselSeq: Any?,
    @SerializedName("webCode")        val webCode: String = "",
    @SerializedName("webTitle")       val webTitle: String = "",
    @SerializedName("name")           val name: String = "",
    @SerializedName("counselHp")      val counselHp: String = "",
    @SerializedName("counselStat")    val counselStat: Int,
    @SerializedName("statusName")     val statusName: String = "",
    @SerializedName("empSeq")         val empSeq: Int? = null,
    @SerializedName("empName")        val empName: String? = null,
    @SerializedName("duplicateState") val duplicateState: String = "N",
    @SerializedName("counselResvDtm") val counselResvDtm: String? = null,
    @SerializedName("regDtm")         val regDtm: String = "",
    @SerializedName("editDtm")        val editDtm: String = "",
    @SerializedName("counselIp")      val counselIp: String? = null,
    @SerializedName("counselSource")  val counselSource: Any? = null,
    @SerializedName("counselMedium")  val counselMedium: Any? = null,
    @SerializedName("counselCampaign") val counselCampaign: Any? = null,
    @SerializedName("counselMemo")    val counselMemo: Any? = null,
    @SerializedName("fieldValues")    val fieldValues: List<CounselDetailFieldValueDto>? = null,
    @SerializedName("logs")           val logs: List<CounselLogDto>? = null,
    @SerializedName("memos")          val memos: List<CounselMemoDto>? = null
) {
    fun toDomain() = CounselDetail(
        counselSeq = counselSeq.toSafeInt(),
        webCode = webCode,
        webTitle = webTitle,
        name = name,
        counselHp = counselHp,
        counselStat = counselStat,
        statusName = statusName,
        empSeq = empSeq,
        empName = empName,
        duplicateState = duplicateState,
        counselResvDtm = counselResvDtm,
        regDtm = regDtm,
        editDtm = editDtm,
        counselIp = counselIp,
        counselSource = counselSource.toSafeString(),
        counselMedium = counselMedium.toSafeString(),
        counselCampaign = counselCampaign.toSafeString(),
        counselMemo = counselMemo.toSafeString(),
        fieldValues = fieldValues?.map { it.toDomain() } ?: emptyList(),
        logs = logs?.map { it.toDomain() } ?: emptyList(),
        memos = memos?.map { it.toDomain() } ?: emptyList()
    )
}

data class CounselDetailFieldValueDto(
    @SerializedName("fieldId")       val fieldId: Int,
    @SerializedName("fieldKey")      val fieldKey: String = "",
    @SerializedName("label")         val label: String = "",
    @SerializedName("fieldType")     val fieldType: String = "",
    @SerializedName("valueText")     val valueText: String? = null,
    @SerializedName("valueNumber")   val valueNumber: Double? = null,
    @SerializedName("valueDate")     val valueDate: String? = null,
    @SerializedName("valueDatetime") val valueDatetime: String? = null
) {
    fun toDomain() = CounselFieldValue(
        fieldId = fieldId,
        fieldKey = fieldKey,
        label = label,
        fieldType = fieldType,
        valueText = valueText,
        valueNumber = valueNumber,
        valueDate = valueDate,
        valueDatetime = valueDatetime
    )
}

data class CounselLogDto(
    @SerializedName("counselSeq")  val counselSeq: Any?,
    @SerializedName("logNo")       val logNo: Int,
    @SerializedName("counselStat") val counselStat: Int,
    @SerializedName("statusName")  val statusName: String = "",
    @SerializedName("regDtm")      val regDtm: String = ""
) {
    fun toDomain() = CounselLog(
        counselSeq = counselSeq.toSafeInt(),
        logNo = logNo,
        counselStat = counselStat,
        statusName = statusName,
        regDtm = regDtm
    )
}

data class CounselMemoDto(
    @SerializedName("memoLogId")   val memoLogId: Any?,
    @SerializedName("counselSeq")  val counselSeq: Any?,
    @SerializedName("statusId")    val statusId: Int,
    @SerializedName("statusName")  val statusName: String = "",
    @SerializedName("memoText")    val memoText: String = "",
    @SerializedName("creatorName") val creatorName: String = "",
    @SerializedName("createdAt")   val createdAt: String = ""
) {
    fun toDomain() = CounselMemo(
        memoLogId = memoLogId.toSafeInt(),
        counselSeq = counselSeq.toSafeInt(),
        statusId = statusId,
        statusName = statusName,
        memoText = memoText,
        creatorName = creatorName,
        createdAt = createdAt
    )
}

// ── 수정 요청 DTO ───────────────────────────────────────────────────────────────

data class CounselUpdateRequest(
    @SerializedName("name")           val name: String? = null,
    @SerializedName("counselHp")      val counselHp: String? = null,
    @SerializedName("empSeq")         val empSeq: Int? = null,
    @SerializedName("counselSource")  val counselSource: String? = null,
    @SerializedName("counselMedium")  val counselMedium: String? = null,
    @SerializedName("counselCampaign") val counselCampaign: String? = null,
    @SerializedName("counselResvDtm") val counselResvDtm: String? = null,
    @SerializedName("counselMemo")    val counselMemo: String? = null,
    @SerializedName("fieldValues")    val fieldValues: List<FieldValueRequest>? = null
)

data class FieldValueRequest(
    @SerializedName("fieldId")       val fieldId: Int,
    @SerializedName("valueText")     val valueText: String? = null,
    @SerializedName("valueNumber")   val valueNumber: Double? = null,
    @SerializedName("valueDate")     val valueDate: String? = null,
    @SerializedName("valueDatetime") val valueDatetime: String? = null
)

// ── 상태 변경 요청 DTO ──────────────────────────────────────────────────────────

data class CounselStatusUpdateRequest(
    @SerializedName("counselStat")    val counselStat: Int,
    @SerializedName("counselResvDtm") val counselResvDtm: String? = null
)

data class CounselMemoRequest(
    @SerializedName("memoText") val memoText: String
)

// ── JSON 파싱을 위한 안전한 확장 함수들 ──────────────────────────────────────────

private fun Any?.toSafeInt(default: Int = 0): Int {
    if (this == null) return default
    if (this is Number) return this.toInt()
    if (this is String) return this.toIntOrNull() ?: default
    val str = this.toString().trim()
    return str.toDoubleOrNull()?.toInt() ?: str.toIntOrNull() ?: default
}

private fun Any?.toSafeString(): String? {
    if (this == null) return null
    if (this is String) return this
    val str = this.toString().trim()
    if (str == "{}" || str.isEmpty()) return null
    return str
}
