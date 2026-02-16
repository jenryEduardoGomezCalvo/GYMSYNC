package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model

/**
 * Respuesta del endpoint POST /auth/register-super-admin
 *
 * Formato de la API:
 * { success: true, message: "Super admin registrado", data: { user: {...}, token: "..." } }
 */
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: RegisterDataDto?
)

data class RegisterDataDto(
    val user: RegisterUserDto,
    val token: String
)

data class RegisterUserDto(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String
)
