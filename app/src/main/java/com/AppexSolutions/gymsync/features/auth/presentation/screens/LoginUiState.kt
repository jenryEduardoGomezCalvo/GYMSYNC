package com.AppexSolutions.gymsync.features.auth.presentation.screens

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession

/**
 * Estado de la pantalla de Login
 *
 * Representa todos los posibles estados:
 * - Idle (inactivo)
 * - Loading (enviando datos)
 * - Success (login exitoso)
 * - Error (credenciales incorrectas, sin internet, etc.)
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val authSession: AuthSession? = null,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoginSuccessful: Boolean = false
)