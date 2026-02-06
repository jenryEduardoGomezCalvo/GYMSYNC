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
    val fechaNacimiento: String = "",

    // Selección de rol
    val rolId: Int = 0,
    val rolNombre: String = "",

    // Selección de gimnasio
    val gymId: Int = 0,
    val gymNombre: String = "",

    // Listas para los dropdowns (hardcodeadas por ahora)
    // TODO: Cargar desde la API cuando esté lista
    val rolesDisponibles: List<Pair<Int, String>> = listOf(
        1 to "Admin",
        2 to "Cliente",
        3 to "Empleado"
    ),
    val gymsDisponibles: List<Pair<Int, String>> = listOf(
        1 to "GymSync Central",
        2 to "GymSync Norte",
        3 to "GymSync Sur"
    )
)
