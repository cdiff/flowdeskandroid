package com.example.flowdesk_android.core.extension

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * UTC ISO-8601 형식의 날짜 문자열(예: 2026-01-26T12:00:00.000Z)을
 * 한국 표준시(KST) 기준 "yyyy. MM. dd." 포맷 문자열로 변환하는 확장 함수
 */
fun String.toFormattedDateString(): String {
    if (this.isBlank()) return ""
    return try {
        val instant = Instant.parse(this)
        val zoneId = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd.", Locale.KOREAN)
        instant.atZone(zoneId).format(formatter)
    } catch (e: Exception) {
        try {
            this.substringBefore("T")
        } catch (ex: Exception) {
            this
        }
    }
}
