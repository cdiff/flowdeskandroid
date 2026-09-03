package com.example.flowdesk_android.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }
    
    fun clear() {
        // 온보딩 노출 여부는 앱 설치 단위로 1회만 보여주기 위해 로그아웃 시에도 유지
        val hasSeenOnboarding = hasSeenOnboarding()
        prefs.edit().clear().apply()
        if (hasSeenOnboarding) {
            setOnboardingSeen(true)
        }
    }

    fun setOnboardingSeen(seen: Boolean = true) {
        prefs.edit().putBoolean("has_seen_onboarding", seen).apply()
    }

    fun hasSeenOnboarding(): Boolean {
        return prefs.getBoolean("has_seen_onboarding", false)
    }

    fun setAutoLogin(enabled: Boolean) {
        prefs.edit().putBoolean("auto_login", enabled).apply()
    }

    fun isAutoLogin(): Boolean {
        return prefs.getBoolean("auto_login", true)
    }
}
