package com.AppexSolutions.gymsync.features.routines.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    companion object {
        private const val TAG = "AlarmScheduler"
        private val DEBUG_FMT = SimpleDateFormat("EEE dd/MM HH:mm:ss", Locale.getDefault())
    }

    /**
     * Programa una alarma por cada día de la rutina.
     *
     * @return true si se programaron alarmas EXACTAS (ideal).
     *         false si se usó fallback inexacto (Android 12+, permiso no concedido)
     *         o si la rutina no tiene días configurados.
     */
    fun schedule(routine: Routine): Boolean {
        if (routine.days.isEmpty()) {
            Log.w(TAG, "Rutina '${routine.name}' sin días — no se programa ninguna alarma")
            return false
        }

        val exactAvailable = canScheduleExact()
        Log.d(TAG, "schedule() — rutina='${routine.name}' id=${routine.id}  exactAvailable=$exactAvailable")

        routine.days.forEach { dayOfWeek ->
            val alarmId = routine.id * 10 + dayOfWeek
            val triggerAtMillis = nextAlarmMillis(dayOfWeek, routine.notificationHour, routine.notificationMinute)
            val pendingIntent = buildPendingIntent(alarmId, routine.id, routine.name)

            scheduleAlarm(alarmId, triggerAtMillis, pendingIntent, exactAvailable, routine.name, dayOfWeek)
        }

        return exactAvailable
    }

    /**
     * Programa una alarma de prueba que dispara en [delaySeconds] segundos.
     * Usar SOLO para verificar que las notificaciones llegan correctamente.
     */
    fun scheduleTest(routineId: Int, routineName: String, delaySeconds: Int = 15) {
        val alarmId = 99_000 + routineId
        val triggerAtMillis = System.currentTimeMillis() + delaySeconds * 1_000L
        val pendingIntent = buildPendingIntent(alarmId, routineId, routineName)
        val humanTime = DEBUG_FMT.format(triggerAtMillis)
        Log.d(TAG, "🧪 Alarma de prueba programada — dispara en ${delaySeconds}s  ($humanTime)")

        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60_000L, pendingIntent)
        }
    }

    /**
     * Cancela todas las alarmas de una rutina.
     */
    fun cancel(routineId: Int, days: List<Int>) {
        days.forEach { dayOfWeek ->
            val alarmId = routineId * 10 + dayOfWeek
            val intent = Intent(context, RoutineNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.d(TAG, "Alarma cancelada — routineId=$routineId  day=$dayOfWeek  alarmId=$alarmId")
            }
        }
    }

    /**
     * Indica si el dispositivo permite alarmas exactas.
     * En Android < 12 siempre retorna true.
     */
    fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private fun scheduleAlarm(
        alarmId: Int,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        useExact: Boolean,
        routineName: String,
        dayOfWeek: Int
    ) {
        val humanTime = DEBUG_FMT.format(triggerAtMillis)

        if (useExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            Log.d(TAG, "⏰ Alarma EXACTA programada — alarmId=$alarmId  rutina='$routineName'  día=$dayOfWeek  dispara=$humanTime")
        } else {
            // Fallback: ventana de 10 minutos (el OS elige el momento exacto dentro de ese rango)
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                10 * 60 * 1000L,
                pendingIntent
            )
            Log.w(
                TAG,
                "⚠️ Alarma INEXACTA programada (sin permiso de alarma exacta) — " +
                "alarmId=$alarmId  rutina='$routineName'  día=$dayOfWeek  ventana=[$humanTime + 10min]"
            )
        }
    }

    private fun buildPendingIntent(alarmId: Int, routineId: Int, routineName: String): PendingIntent {
        val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
            putExtra("routineId", routineId)
            putExtra("routineName", routineName)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Calcula el epoch ms del próximo [dayOfWeek] a [hour]:[minute].
     * dayOfWeek: 1=Lun … 7=Dom (ISO 8601).
     * Si la hora ya pasó esta semana, devuelve la de la semana siguiente.
     */
    private fun nextAlarmMillis(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val calDay = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }
}
