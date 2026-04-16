package com.AppexSolutions.gymsync.features.notifications.data.remote

import com.google.gson.annotations.SerializedName

// ── FCM Token Registration ──────────────────────────────────────────────────

/**
 * Body para PATCH /users/{id}/fcm-token
 * { "fcm_token": "..." }
 */
data class FcmTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)

/**
 * Respuesta de PATCH /users/{id}/fcm-token
 * { "success": true, "message": "Token FCM actualizado", "data": null }
 */
data class FcmTokenResponse(
    val success: Boolean,
    val message: String
)

// ── Broadcast ───────────────────────────────────────────────────────────────

/**
 * Body del endpoint POST /notifications/broadcast.
 * El backend resuelve destinatarios y tokens server-side a partir del gym del admin.
 *
 * Ejemplo: { "title": "Cierre mañana", "message": "El gimnasio cerrará a las 8pm" }
 */
data class BroadcastRequestDto(
    val title: String,
    val message: String
)

/** Data anidada dentro del envelope de respuesta del broadcast. */
data class BroadcastResultDto(
    @SerializedName("sent")
    val sent: Int
)

/**
 * Respuesta del broadcast:
 * { "success": true, "message": "Notificación enviada a X usuarios", "data": { "sent": N } }
 */
data class BroadcastResponseDto(
    val success: Boolean,
    val message: String,
    val data: BroadcastResultDto?
)
