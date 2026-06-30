package com.example.flowdesk_android.feature.counsel_management.data.dto

import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselFieldValue
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselItem
import com.example.flowdesk_android.feature.counsel_management.domain.model.CounselList
import com.example.flowdesk_android.feature.counsel_management.domain.model.PageInfo
import com.google.gson.annotations.SerializedName

data class CounselListResponse(
    @SerializedName("items")    val items: List<CounselItemDto>?,
    @SerializedName("pageInfo") val pageInfo: CounselPageInfoDto?
) {
    fun toDomain() = CounselList(
        items = items?.map { it.toDomain() } ?: emptyList(),
        pageInfo = pageInfo?.toDomain()
    )
}

data class CounselItemDto(
    @SerializedName("counselSeq")     val counselSeq: Int,
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
    @SerializedName("fieldValues")    val fieldValues: List<CounselFieldValueDto>? = null
) {
    fun toDomain() = CounselItem(
        counselSeq = counselSeq,
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
        fieldValues = fieldValues?.map { it.toDomain() } ?: emptyList()
    )
}

data class CounselFieldValueDto(
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

data class CounselPageInfoDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("pageSize")    val pageSize: Int = 20,
    @SerializedName("totalItems")   val totalItems: Int = 0,
    @SerializedName("totalPages")  val totalPages: Int = 0
) {
    fun toDomain() = PageInfo(
        currentPage = currentPage,
        pageSize = pageSize,
        totalItems = totalItems,
        totalPages = totalPages
    )
}
