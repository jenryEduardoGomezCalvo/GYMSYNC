package com.AppexSolutions.gymsync.features.routines.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.AppexSolutions.gymsync.R

class RoutineNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "routine_reminders_v2"
        private const val TAG = "RoutineReceiver"

        private val MOTIVATIONAL_MESSAGES = listOf(
            "¡Hoy es un buen día para moverse! 💪",
            "Recuerda: el mejor entrenamiento es el que haces.",
            "Un día de descanso activo también cuenta.",
            "¿Ya tomaste suficiente agua hoy?",
            "Pequeños pasos, grandes resultados."
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "✅ onReceive disparado — alarma recibida correctamente")

        // Crear canal antes de mostrar la notificación (idempotente en Android 8+)
        ensureChannelExists(context)

        val routineName = intent.getStringExtra("routineName")
        val notificationId = intent.getIntExtra("routineId", System.currentTimeMillis().toInt())

        Log.d(TAG, "routineId=$notificationId  routineName=$routineName")

        val (title, body) = if (!routineName.isNullOrBlank()) {
            "¡Hora de entrenar!" to "Tienes una rutina hoy: $routineName"
        } else {
            "GymSync" to MOTIVATIONAL_MESSAGES.random()
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            // PRIORITY_HIGH + DEFAULT_ALL necesarios para heads-up (pop-up) en todos los OEM
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "📣 Notificación enviada — id=$notificationId  título='$title'")
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS no concedido (Android 13+)
            Log.e(TAG, "❌ SecurityException — POST_NOTIFICATIONS no concedido: ${e.message}")
        }
    }

    /**
     * Crea (o recrea con IMPORTANCE_HIGH) el canal de rutinas.
     * Es idempotente: si ya existe con la importancia correcta, no hace nada.
     */
    private fun ensureChannelExists(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        // Limpiar canal viejo "routines_channel" si aún existe
        if (manager.getNotificationChannel("routines_channel") != null) {
            manager.deleteNotificationChannel("routines_channel")
            Log.d(TAG, "Canal viejo 'routines_channel' eliminado")
        }

        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null && existing.importance >= NotificationManager.IMPORTANCE_HIGH) {
            return // canal ya existe con la importancia correcta
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rutinas de ejercicio",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorios de tus rutinas de entrenamiento"
            enableVibration(true)
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
        Log.d(TAG, "Canal '$CHANNEL_ID' creado con IMPORTANCE_HIGH")
    }
}
