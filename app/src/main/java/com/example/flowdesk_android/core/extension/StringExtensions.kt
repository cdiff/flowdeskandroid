package com.example.flowdesk_android.core.extension

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * UTC ISO-8601 형식의 날짜 문자열(예: 2026-01-26T12:00:00.000Z)을
 * 한국 표준시(KST) 및 사용자가 읽기 편한 포맷("yyyy. MM. dd. a hh:mm")의 문자열로 변환하는 확장 함수
 */
fun String.toFormattedDateString(): String {
    if (this.isBlank()) return ""
    return try {
        val instant = Instant.parse(this)
        val zoneId = ZoneId.systemDefault() // 시스템 기본 로컬 타임존 반영
        val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. a hh:mm", Locale.KOREAN)
        instant.atZone(zoneId).format(formatter)
    } catch (e: Exception) {
        // 파싱 실패 시, T 구분자를 공백으로 치환하고 밀리초 부분을 잘라내는 기본 폴백 적용
        try {
            val formatted = this.replace("T", " ").substringBefore(".")
            if (formatted.length >= 16) formatted.substring(0, 16) else formatted
        } catch (ex: Exception) {
            this
        }
    }
}
