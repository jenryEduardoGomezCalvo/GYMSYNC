package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.util.Log
import com.AppexSolutions.gymsync.features.notifications.domain.repository.FcmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFcmTokenUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "FCM"
    }

    /**
     * Envía el token FCM al backend para asociarlo con el usuario autenticado.
     * Se llama desde onNewToken() (token rotado por Firebase) y desde InitializeFcmUseCase
     * (post-login para garantizar que el token esté registrado).
     */
    operator fun invoke(token: String) {
        Log.d(TAG, "Registrando nuevo token FCM: ${token.take(20)}...")
        scope.launch {
            fcmRepository.updateFcmToken(token)
                .onSuccess { Log.d(TAG, "Token FCM enviado al backend correctamente") }
                .onFailure { Log.e(TAG, "Error enviando token FCM al backend", it) }
        }
    }
}
