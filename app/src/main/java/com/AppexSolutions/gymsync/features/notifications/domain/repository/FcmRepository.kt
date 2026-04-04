package com.AppexSolutions.gymsync.features.notifications.domain.repository

/**
 * Repository para manejar operaciones relacionadas con FCM
 */
interface FcmRepository {
    /**
     * Actualiza el token FCM del usuario actual en el backend
     */
    suspend fun updateFcmToken(token: String): Result<Unit>

    /**
     * Guarda el token FCM localmente para el usuario actual
     */
    suspend fun saveFcmTokenLocally(token: String): Result<Unit>

    /**
     * Obtiene el token FCM guardado localmente
     */
    suspend fun getStoredFcmToken(): String?
}
