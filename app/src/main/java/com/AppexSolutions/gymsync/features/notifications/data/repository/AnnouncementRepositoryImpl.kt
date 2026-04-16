package com.AppexSolutions.gymsync.features.notifications.data.repository

import com.AppexSolutions.gymsync.core.datastore.AnnouncementDao
import com.AppexSolutions.gymsync.core.datastore.AnnouncementEntity
import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.notifications.data.remote.BroadcastRequestDto
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastRecipient
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastResult
import com.AppexSolutions.gymsync.features.notifications.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepositoryImpl @Inject constructor(
    private val api: GymSyncAPI,
    private val dao: AnnouncementDao
) : AnnouncementRepository {

    /** rolId del cliente según la lógica actual del proyecto. */
    private val clientRolId: Int = 4

    override suspend fun getBroadcastRecipients(): Result<List<BroadcastRecipient>> =
        runCatching {
            api.getAllUsers().data
                .filter { it.rolId == clientRolId }
                .map { dto ->
                    BroadcastRecipient(
                        userId = dto.id,
                        displayName = "${dto.nombres} ${dto.apellidos}".trim(),
                        fcmToken = dto.fcmToken,
                        // Si el backend aún no expone el flag, asumimos suscrito.
                        receivesNotifications = dto.receivesNotifications ?: true
                    )
                }
        }

    override suspend fun sendBroadcast(
        title: String,
        message: String,
        type: AnnouncementType,
        sentBy: String
    ): Result<BroadcastResult> = runCatching {
        require(title.isNotBlank()) { "El título es obligatorio." }
        require(message.isNotBlank()) { "El mensaje es obligatorio." }
        require(title.length <= 100) { "El título supera 100 caracteres." }
        require(message.length <= 500) { "El mensaje supera 500 caracteres." }

        val response = api.broadcastAnnouncement(
            BroadcastRequestDto(
                title = title,
                message = message
            )
        )
        val data = response.data
        if (!response.success || data == null) {
            error(response.message.ifBlank { "Broadcast falló." })
        }

        val now = System.currentTimeMillis()

        // Guardar historial local en el dispositivo del admin.
        dao.insert(
            AnnouncementEntity(
                title = title,
                message = message,
                sentAt = now,
                sentBy = sentBy,
                recipientCount = data.sent,
                type = type,
                isRead = true
            )
        )

        BroadcastResult(sentCount = data.sent)
    }

    override suspend fun persistReceivedAnnouncement(
        title: String,
        message: String,
        type: AnnouncementType,
        sentAt: Long,
        sentBy: String
    ) {
        dao.insert(
            AnnouncementEntity(
                title = title,
                message = message,
                sentAt = sentAt,
                sentBy = sentBy,
                // Para el cliente no conocemos recipientCount del broadcast;
                // guardamos 1 (él mismo).
                recipientCount = 1,
                type = type,
                isRead = false
            )
        )
    }

    override fun observeAnnouncements(): Flow<List<Announcement>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    private fun AnnouncementEntity.toDomain() = Announcement(
        id = id,
        title = title,
        message = message,
        sentAt = sentAt,
        sentBy = sentBy,
        recipientCount = recipientCount,
        type = type,
        isRead = isRead
    )
}
