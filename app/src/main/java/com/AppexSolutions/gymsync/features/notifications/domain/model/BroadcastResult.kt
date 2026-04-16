package com.AppexSolutions.gymsync.features.notifications.domain.model

/** Resultado de un envío broadcast: cuántos usuarios recibieron la notificación. */
data class BroadcastResult(
    val sentCount: Int
)

/**
 * Representación mínima de un destinatario para broadcast.
 * `fcmToken` es null si el usuario aún no ha registrado su dispositivo.
 */
data class BroadcastRecipient(
    val userId: Int,
    val displayName: String,
    val fcmToken: String?,
    val receivesNotifications: Boolean
)
