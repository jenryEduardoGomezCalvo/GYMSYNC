package com.AppexSolutions.gymsync.features.notifications.data.repository

import android.util.Log
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
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
            // Obtener usuario actual con token
            val currentUser = userDao.getUserWithFcmToken()
                ?: userDao.getLastBiometricUser()
                ?: return@withContext Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )

            // Guardar localmente primero
            userDao.updateFcmToken(currentUser.email, token)

            // Enviar al backend (cuando tengas el endpoint)
            // val response = api.updateFcmToken(
            //     userId = currentUser.id,
            //     token = token
            // )

            Log.d(TAG, "FCM Token actualizado para usuario: ${currentUser.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando FCM token", e)
            Result.failure(e)
        }
    }

    override suspend fun saveFcmTokenLocally(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = userDao.getUserWithFcmToken()
                ?: userDao.getLastBiometricUser()

            currentUser?.let {
                userDao.updateFcmToken(it.email, token)
                Log.d(TAG, "FCM Token guardado localmente")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando FCM token", e)
            Result.failure(e)
        }
    }

    override suspend fun getStoredFcmToken(): String? = withContext(Dispatchers.IO) {
        userDao.getUserWithFcmToken()?.fcmToken
    }
}
