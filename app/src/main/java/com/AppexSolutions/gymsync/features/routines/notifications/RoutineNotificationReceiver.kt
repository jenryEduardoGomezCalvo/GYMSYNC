package com.AppexSolutions.gymsync.features.routines.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.AppexSolutions.gymsync.R

class RoutineNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "routines_channel"

        private val MOTIVATIONAL_MESSAGES = listOf(
            "¡Hoy es un buen día para moverse! 💪",
            "Recuerda: el mejor entrenamiento es el que haces.",
            "Un día de descanso activo también cuenta.",
            "¿Ya tomaste suficiente agua hoy?",
            "Pequeños pasos, grandes resultados."
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val routineName = intent.getStringExtra("routineName")
        val notificationId = intent.getIntExtra("routineId", System.currentTimeMillis().toInt())

        val (title, body) = if (!routineName.isNullOrBlank()) {
            "¡Hora de entrenar!" to "Tienes una rutina hoy: $routineName"
        } else {
            "GymSync" to MOTIVATIONAL_MESSAGES.random()
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS no concedido — ignorar silenciosamente
        }
    }
}
