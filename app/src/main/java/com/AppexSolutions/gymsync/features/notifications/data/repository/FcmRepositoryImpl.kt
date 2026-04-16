package com.AppexSolutions.gymsync.features.notifications.data.repository

import android.util.Log
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.notifications.data.remote.FcmTokenRequest
import com.AppexSolutions.gymsync.features.notifications.domain.repository.FcmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepositoryImpl @Inject constructor(
    private val api: GymSyncAPI,
    private val userDao: UserDao
) : FcmRepository {

    companion object {
        private const val TAG = "FcmRepository"
    }

    override suspend fun updateFcmToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = userDao.getActiveUser()
                ?: userDao.getLastBiometricUser()
                ?: return@withContext Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )

            // 1. Guardar token localmente
            userDao.updateFcmToken(currentUser.email, token)
            Log.d(TAG, "Token FCM guardado localmente para: ${currentUser.email}")

            // 2. Enviar al backend via PATCH /users/{id}/fcm-token
            val backendId = currentUser.backendId
            if (backendId == 0) {
                Log.w(TAG, "backendId es 0 — el token se guardó localmente pero no se envió al servidor")
                return@withContext Result.failure(
                    IllegalStateException("backendId no disponible, re-login requerido")
                )
            }

            val response = api.updateFcmToken(
                userId = backendId,
                body = FcmTokenRequest(fcmToken = token)
            )

            if (response.success) {
                Log.d(TAG, "Token FCM registrado en backend para userId=$backendId: ${response.message}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Backend rechazó token FCM: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando FCM token", e)
            Result.failure(e)
        }
    }

    override suspend fun saveFcmTokenLocally(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = userDao.getActiveUser()
                ?: userDao.getLastBiometricUser()

            currentUser?.let {
                userDao.updateFcmToken(it.email, token)
                Log.d(TAG, "Token FCM guardado localmente para: ${it.email}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando token FCM localmente", e)
            Result.failure(e)
        }
    }

    override suspend fun getStoredFcmToken(): String? = withContext(Dispatchers.IO) {
        userDao.getUserWithFcmToken()?.fcmToken
    }
}
