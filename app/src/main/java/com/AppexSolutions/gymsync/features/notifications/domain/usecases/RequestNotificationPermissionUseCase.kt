package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// TODO: Descomentar cuando actives Firebase
// import com.google.firebase.messaging.FirebaseMessaging
// import kotlinx.coroutines.suspendCancellableCoroutine
// import kotlin.coroutines.resume

class RequestNotificationPermissionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Verifica si tenemos permiso de notificaciones (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Antes de Android 13 no se necesita permiso explícito
        }
    }

    // TODO: Descomentar cuando actives Firebase
    /*
    /**
     * Obtiene el token FCM actual.
     */
    suspend fun getCurrentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                continuation.resume(token)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
    */
}