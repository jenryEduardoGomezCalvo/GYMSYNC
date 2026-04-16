package com.AppexSolutions.gymsync.core.datastore

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tipo de anuncio enviado por el administrador del gimnasio.
 * Determina el canal/estilo de notificación y el filtro en el historial.
 */
enum class AnnouncementType { GENERAL, URGENTE, PROMOCION }

/**
 * Anuncio persistido localmente.
 *
 * - En el dispositivo del ADMIN: representa anuncios enviados (historial).
 *   `recipientCount` = cuántos tokens recibieron el push (sentCount del backend).
 *   `sentBy` = email/nombre del admin que lo envió.
 *
 * - En el dispositivo del CLIENTE: representa anuncios recibidos vía FCM.
 *   `isRead` arranca en false; se marca true al abrir NotificationsScreen.
 */
@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "sent_at")
    val sentAt: Long,

    @ColumnInfo(name = "sent_by")
    val sentBy: String,

    @ColumnInfo(name = "recipient_count")
    val recipientCount: Int,

    @ColumnInfo(name = "type")
    val type: AnnouncementType,

    @ColumnInfo(name = "is_read", defaultValue = "0")
    val isRead: Boolean = false
)
