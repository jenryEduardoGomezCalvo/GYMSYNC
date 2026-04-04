package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.util.Log
import com.AppexSolutions.gymsync.core.datastore.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFcmTokenUseCase @Inject constructor(
    private val userDao: UserDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "FCM"
    }

    /**
     * Envía el token FCM al backend para asociarlo con el usuario.
     * Se ejecuta cada vez que FCM genera un nuevo token.
     */
    operator fun invoke(token: String) {
        scope.launch {
            try {
                // Guardar token localmente
                val user = userDao.getUserWithFcmToken()
                user?.let {
                    userDao.updateFcmToken(it.email, token)
                    Log.d(TAG, "Token FCM actualizado para: ${it.email}")
                }

                // Aquí se enviaría al backend
                // userRepository.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando token FCM", e)
            }
        }
    }
}
