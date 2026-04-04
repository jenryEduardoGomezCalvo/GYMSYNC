package com.AppexSolutions.gymsync.features.notifications.service

// TODO: Descomentar cuando actives Firebase
/*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.AppexSolutions.gymsync.MainActivity
import com.AppexSolutions.gymsync.R
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.ProcessFcmMessageUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.UpdateFcmTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GymSyncMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var processFcmMessageUseCase: ProcessFcmMessageUseCase

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    companion object {
        const val CHANNEL_GENERAL = "gymsync_general"
        const val CHANNEL_MEMBERSHIP = "gymsync_membership"
        const val CHANNEL_PROMOTIONS = "gymsync_promotions"
        const val TAG = "GymSyncFCM"

        // Actions para Data Messages
        const val ACTION_MEMBERSHIP_EXPIRING = "membership_expiring"
        const val ACTION_MEMBERSHIP_EXPIRED = "membership_expired"
        const val ACTION_ATTENDANCE_CONFIRMED = "attendance_confirmed"
        const val ACTION_QR_REFRESH = "qr_refresh"
        const val ACTION_MEMBERSHIP_UPDATED = "membership_updated"
        const val ACTION_GYM_ANNOUNCEMENT = "gym_announcement"
        const val ACTION_PAYMENT_CONFIRMED = "payment_confirmed"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${message.from}")
        Log.d(TAG, "Tipo de mensaje: ${message.messageType}")
        Log.d(TAG, "Data payload: ${message.data}")

        val action = message.data["action"]
        val navigateTo = message.data["navigateTo"]

        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "GYMSYNC",
                body = notification.body ?: "",
                channelId = getChannelForAction(action),
                data = message.data
            )
        }

        if (message.data.isNotEmpty()) {
            processDataMessage(message.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo FCM Token: $token")
        updateFcmTokenUseCase(token)
    }

    private fun processDataMessage(data: Map<String, String>) {
        val action = data["action"] ?: return

        when (action) {
            ACTION_MEMBERSHIP_EXPIRING -> {
                val daysLeft = data["daysLeft"] ?: "3"
                val userName = data["userName"] ?: "Usuario"
                showNotification(
                    title = "Tu membresía vence pronto",
                    body = "Hola $userName, te quedan $daysLeft días. Renueva ahora y mantén tu acceso al gimnasio.",
                    channelId = CHANNEL_MEMBERSHIP,
                    data = data
                )
                processFcmMessageUseCase.updateMembership(data)
            }
            ACTION_MEMBERSHIP_EXPIRED -> {
                showNotification(
                    title = "Tu membresía ha expirado",
                    body = "Renueva ahora para recuperar el acceso al gimnasio. ¡Te esperamos!",
                    channelId = CHANNEL_MEMBERSHIP,
                    data = data
                )
            }
            ACTION_PAYMENT_CONFIRMED -> {
                showNotification(
                    title = "Pago confirmado",
                    body = "Tu pago ha sido procesado exitosamente. ¡Gracias por confiar en nosotros!",
                    channelId = CHANNEL_GENERAL,
                    data = data
                )
                processFcmMessageUseCase.updateMembership(data)
            }
            ACTION_ATTENDANCE_CONFIRMED -> {
                processFcmMessageUseCase.syncAttendance(data)
            }
            ACTION_QR_REFRESH -> {
                processFcmMessageUseCase.refreshQrCode(data)
            }
            ACTION_MEMBERSHIP_UPDATED -> {
                processFcmMessageUseCase.updateMembership(data)
            }
            ACTION_GYM_ANNOUNCEMENT -> {
                val title = data["announcementTitle"] ?: "Anuncio del gimnasio"
                val body = data["announcementBody"] ?: ""
                showNotification(
                    title = title,
                    body = body,
                    channelId = CHANNEL_GENERAL,
                    data = data
                )
                processFcmMessageUseCase.handleAnnouncement(data)
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data["navigateTo"]?.let { putExtra("navigate_to", it) }
            data["clientId"]?.let { putExtra("client_id", it) }
            data["action"]?.let { putExtra("action", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getChannelForAction(action: String?): String {
        return when (action) {
            ACTION_MEMBERSHIP_EXPIRING,
            ACTION_MEMBERSHIP_EXPIRED,
            ACTION_MEMBERSHIP_UPDATED -> CHANNEL_MEMBERSHIP
            ACTION_PAYMENT_CONFIRMED,
            ACTION_GYM_ANNOUNCEMENT -> CHANNEL_GENERAL
            else -> CHANNEL_GENERAL
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "Notificaciones Generales",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Anuncios y notificaciones importantes" },

                NotificationChannel(
                    CHANNEL_MEMBERSHIP,
                    "Membresía",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Recordatorios de vencimiento y renovación de membresía"
                    enableVibration(true)
                },

                NotificationChannel(
                    CHANNEL_PROMOTIONS,
                    "Promociones",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Ofertas y promociones de membresía" }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
        }
    }
}
*/