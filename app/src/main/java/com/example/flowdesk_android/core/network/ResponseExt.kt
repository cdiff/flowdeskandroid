package com.example.flowdesk_android.core.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

/**
 * Retrofit Response의 errorBody를 파싱하여 서버의 error.message를 추출합니다.
 *
 * 서버 에러 응답 구조:
 * {
 *   "error": { "code": "RES001", "message": "게시판을 찾을 수 없습니다.", "statusCode": 404 },
 *   "meta": { ... }
 * }
 *
 * @param fallback errorBody 파싱 실패 시 반환할 기본 메시지
 */
fun <T> Response<T>.parseErrorMessage(fallback: String = "오류가 발생했습니다."): String {
    return try {
        val errorJson = errorBody()?.string() ?: return fallback
        val root = JSONObject(errorJson)
        root.optJSONObject("error")?.optString("message")?.takeIf { it.isNotEmpty() }
            ?: fallback
    } catch (e: JSONException) {
        fallback
    }
}
