package com.example.flowdesk_android.feature.auth.domain.model

data class AuthUser(
    val id: String,
    val userId: String,
    val email: String,
    val name: String,
    val corpName: String,
    val token: String = "",
    val tel: String? = null,
    val hp: String? = null
)

data class AuthMeInfo(
    val user: AuthUser,
    val roles: List<String>,
    val permissions: Map<String, Boolean>,
    val menuTree: List<Menu>
)

data class Menu(
    val pageName: String,
    val displayName: String,
    val path: String,
    val order: Int,
    val children: List<Menu>
)

enum class AuthProvider {
    CREDENTIALS,
    GOOGLE
}

data class LoginCommand(
    val provider: AuthProvider,
    val tenantName: String,
    val userId: String,
    val secret: String
)

sealed interface AuthSession {
    object Guest : AuthSession
    data class Active(val user: AuthUser) : AuthSession
}

