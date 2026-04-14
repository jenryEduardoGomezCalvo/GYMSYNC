package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.notifications.RoutineAlarmScheduler
import javax.inject.Inject

class CancelRoutineAlarmsUseCase @Inject constructor(
    private val scheduler: RoutineAlarmScheduler
) {
    operator fun invoke(routineId: Int, days: List<Int>) = scheduler.cancel(routineId, days)
}
