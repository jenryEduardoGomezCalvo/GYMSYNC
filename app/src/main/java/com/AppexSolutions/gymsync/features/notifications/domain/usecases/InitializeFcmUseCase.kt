package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase para inicializar FCM después del login.
 * Obtiene el token FCM y lo envía al backend vía PATCH /users/{id}/fcm-token.
 *
 * NOTA: El registro del token en el backend es independiente del permiso
 * POST_NOTIFICATIONS. El permiso solo controla si el OS muestra las notificaciones.
 */
@Singleton
class InitializeFcmUseCase @Inject constructor(
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase
) {
    companion object {
        private const val TAG = "InitializeFcm"
    }

    /**
     * Inicializa FCM para el usuario actual.
     * Debe llamarse después de un login exitoso.
     */
    suspend operator fun invoke(): Result<String> {
        return try {
            if (!requestNotificationPermissionUseCase.hasNotificationPermission()) {
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido — las notificaciones no se mostrarán en pantalla")
            }

            // Obtener token FCM y enviarlo al backend
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM obtenido: ${token.take(20)}...")

            updateFcmTokenUseCase(token)

            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando FCM", e)
            Result.failure(e)
        }
    }
}
