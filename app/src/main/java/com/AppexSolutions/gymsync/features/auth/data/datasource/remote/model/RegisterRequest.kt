package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model

/**
 * Request para el endpoint POST /auth/register-super-admin
 * Coincide con el RegisterDto de la API
 */
data class RegisterRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val password: String,
    val telefono: String?,
    val fechaNacimiento: String?  // Formato: "YYYY-MM-DD"
)
