package com.AppexSolutions.gymsync.features.notifications.domain.repository

import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastRecipient
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato único para el sistema de anuncios FCM.
 * El mismo repo se usa en admin (enviar/historial) y cliente (recibir).
 */
interface AnnouncementRepository {

    /** Lista de destinatarios disponibles para broadcast (clientes con token y suscritos). */
    suspend fun getBroadcastRecipients(): Result<List<BroadcastRecipient>>

    /**
     * Envía el broadcast vía backend. El backend resuelve destinatarios server-side.
     * Si ok, persiste un registro en el historial local.
     * `sentBy` normalmente es el nombre/email del admin actual.
     */
    suspend fun sendBroadcast(
        title: String,
        message: String,
        type: AnnouncementType,
        sentBy: String
    ): Result<BroadcastResult>

    /** Guarda un anuncio recibido por FCM en el dispositivo del cliente. */
    suspend fun persistReceivedAnnouncement(
        title: String,
        message: String,
        type: AnnouncementType,
        sentAt: Long,
        sentBy: String
    )

    /** Historial de anuncios (enviados o recibidos, según el rol del dispositivo). */
    fun observeAnnouncements(): Flow<List<Announcement>>

    /** Contador de anuncios no leídos (para badge del cliente). */
    fun observeUnreadCount(): Flow<Int>

    suspend fun markAsRead(id: Int)

    suspend fun markAllAsRead()
}
