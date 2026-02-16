package com.AppexSolutions.gymsync.features.auth.domain.entities

/**
 * Entidad de dominio para el registro de usuario
 *
 * Mismo patrón que User (login) — encapsula todos los datos del registro
 */
data class RegisterUser(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val password: String,
    val telefono: String?,
    val fechaNacimiento: String?  // Formato: "YYYY-MM-DD"
)
