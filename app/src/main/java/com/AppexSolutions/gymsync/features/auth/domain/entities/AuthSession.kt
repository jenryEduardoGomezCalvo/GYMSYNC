package com.AppexSolutions.gymsync.features.auth.domain.entities

data class AuthSession(
    val token: String,
    val id_user: Any,
    val roleName: String? = null
)