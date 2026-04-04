package com.AppexSolutions.gymsync.features.notifications.domain.usecases

// TODO: Descomentar cuando actives Firebase
/*
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase para inicializar FCM después del login.
 * Obtiene el token FCM y lo guarda asociado al usuario.
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
            // Verificar permisos primero
            if (!requestNotificationPermissionUseCase.hasNotificationPermission()) {
                return Result.failure(Exception("No hay permiso de notificaciones"))
            }

            // Obtener token FCM
            val token = FirebaseMessaging.getInstance().token.await()

            // Guardar token
            updateFcmTokenUseCase(token)

            Log.d(TAG, "FCM inicializado correctamente. Token: $token")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando FCM", e)
            Result.failure(e)
        }
    }
}
*/
