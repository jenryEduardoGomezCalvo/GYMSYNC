package com.AppexSolutions.gymsync.features.notifications.domain.model

import com.AppexSolutions.gymsync.core.datastore.AnnouncementType

/**
 * Modelo de dominio de un anuncio (enviado o recibido).
 *
 * - Lado admin: proviene del historial (Room) tras enviar broadcast.
 * - Lado cliente: proviene de anuncios recibidos vía FCM y persistidos en Room.
 */
data class Announcement(
    val id: Int,
    val title: String,
    val message: String,
    val sentAt: Long,
    val sentBy: String,
    val recipientCount: Int,
    val type: AnnouncementType,
    val isRead: Boolean
)
