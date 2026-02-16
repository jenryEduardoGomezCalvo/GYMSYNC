package com.AppexSolutions.gymsync.features.auth.presentation.screens

/**
 * Estado de la pantalla de Registro
 *
 * Representa todos los posibles estados:
 * - Idle (inactivo)
 * - Loading (enviando datos)
 * - Success (registro exitoso)
 * - Error (validación fallida, sin internet, etc.)
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegisterSuccessful: Boolean = false,

    // Campos del formulario
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = ""
)
