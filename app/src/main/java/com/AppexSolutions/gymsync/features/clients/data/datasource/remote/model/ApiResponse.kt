package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model

/**
 * Respuesta genérica de la API para operaciones sin data
 * Ejemplo: DELETE /users/:id -> { success: true, message: "Usuario eliminado", data: null }
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
