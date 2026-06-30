package com.example.flowdesk_android.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    /**
     * ISO-8601 형식의 날짜 문자열(예: 2026-01-26T12:00:00.000Z)을
     * "yyyy.MM.dd HH:mm" 형태의 사람이 보기 쉬운 로컬 시간 문자열로 변환합니다.
     */
    fun formatIsoDate(isoStr: String?): String {
        if (isoStr.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(isoStr)
            val zoneId = ZoneId.systemDefault()
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            instant.atZone(zoneId).format(formatter)
        } catch (e: Exception) {
            isoStr.replace("T", " ").substringBefore(".")
        }
    }
}
