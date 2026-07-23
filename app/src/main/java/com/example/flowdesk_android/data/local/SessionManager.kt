package com.example.flowdesk_android.data.local

import com.example.flowdesk_android.feature.auth.domain.model.AuthMeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    private val _sessionState = MutableStateFlow<AuthMeInfo?>(null)
    val sessionState: StateFlow<AuthMeInfo?> = _sessionState.asStateFlow()

    fun setSession(info: AuthMeInfo) {
        _sessionState.value = info
    }

    fun clear() {
        _sessionState.value = null
    }

    fun getSession(): AuthMeInfo? {
        return _sessionState.value
    }

    fun observePermission(permissionKey: String): Flow<Boolean> {
        return sessionState.map { info ->
            info?.permissions?.get(permissionKey) == true
        }
    }

    fun hasPermission(permissionKey: String): Boolean {
        return _sessionState.value?.permissions?.get(permissionKey) == true
    }
}
