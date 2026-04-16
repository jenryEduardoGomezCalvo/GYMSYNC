package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastRecipient
import com.AppexSolutions.gymsync.features.notifications.domain.model.BroadcastResult
import com.AppexSolutions.gymsync.features.notifications.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Devuelve los clientes elegibles para broadcast (filtrados por token + suscripción). */
class GetBroadcastRecipientsUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    suspend operator fun invoke(): Result<List<BroadcastRecipient>> = repo.getBroadcastRecipients()

    /** Sólo los que pueden recibir el push (token no nulo y receivesNotifications=true). */
    suspend fun eligibleOnly(): Result<List<BroadcastRecipient>> =
        repo.getBroadcastRecipients().map { list ->
            list.filter { it.receivesNotifications && !it.fcmToken.isNullOrBlank() }
        }
}

/** Envía el broadcast y persiste historial local. */
class SendBroadcastAnnouncementUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    suspend operator fun invoke(
        title: String,
        message: String,
        type: AnnouncementType,
        sentBy: String
    ): Result<BroadcastResult> = repo.sendBroadcast(title, message, type, sentBy)
}

class ObserveAnnouncementsUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    operator fun invoke(): Flow<List<Announcement>> = repo.observeAnnouncements()
}

class ObserveUnreadAnnouncementCountUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    operator fun invoke(): Flow<Int> = repo.observeUnreadCount()
}

class MarkAnnouncementAsReadUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    suspend operator fun invoke(id: Int) = repo.markAsRead(id)
}

class MarkAllAnnouncementsAsReadUseCase @Inject constructor(
    private val repo: AnnouncementRepository
) {
    suspend operator fun invoke() = repo.markAllAsRead()
}
