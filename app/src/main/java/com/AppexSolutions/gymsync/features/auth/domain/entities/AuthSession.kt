package com.AppexSolutions.gymsync.features.auth.domain.entities

data class AuthSession(
    val token : String,
    val id_user : Int
)

data class FAuthSession(
    val token: String,
    val userId: Int,
    val email: String,
    val name: String?,
    val expiresAt: Long?
)