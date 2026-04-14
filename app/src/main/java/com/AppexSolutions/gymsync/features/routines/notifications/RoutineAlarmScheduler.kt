package com.AppexSolutions.gymsync.features.routines.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(routine: Routine) {
        routine.days.forEach { dayOfWeek ->
            val alarmId = routine.id * 10 + dayOfWeek
            val triggerAtMillis = nextAlarmMillis(
                dayOfWeek,
                routine.notificationHour,
                routine.notificationMinute
            )
            val pendingIntent = buildPendingIntent(alarmId, routine.id, routine.name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        }
    }

    fun cancel(routineId: Int, days: List<Int>) {
        days.forEach { dayOfWeek ->
            val alarmId = routineId * 10 + dayOfWeek
            val intent = Intent(context, RoutineNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun buildPendingIntent(alarmId: Int, routineId: Int, routineName: String): PendingIntent {
        val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
            putExtra("routineId", routineId)
            putExtra("routineName", routineName)
        }
        return PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Calcula el epoch ms del próximo [dayOfWeek] a [hour]:[minute].
     * dayOfWeek: 1=Lun … 7=Dom (ISO 8601).
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
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }
}
