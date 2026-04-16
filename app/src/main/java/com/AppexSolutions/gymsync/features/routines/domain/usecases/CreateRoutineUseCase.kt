package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import javax.inject.Inject

/** Resultado de crear una rutina. */
data class CreateRoutineResult(
    val routineId: Int,
    /** true = alarma exacta programada; false = alarma inexacta o sin permiso */
    val exactAlarmScheduled: Boolean
)

class CreateRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val scheduleAlarms: ScheduleRoutineAlarmsUseCase
) {
    suspend operator fun invoke(
        name: String,
        userId: Int,
        days: List<Int>,
        notificationHour: Int,
        notificationMinute: Int
    ): CreateRoutineResult {
        val routine = Routine(
            id = 0,
            userId = userId,
            name = name,
            days = days,
            notificationHour = notificationHour,
            notificationMinute = notificationMinute,
            exercises = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        val id = repository.createRoutine(routine)
        val exactAlarm = scheduleAlarms(routine.copy(id = id))
        return CreateRoutineResult(routineId = id, exactAlarmScheduled = exactAlarm)
    }
}
