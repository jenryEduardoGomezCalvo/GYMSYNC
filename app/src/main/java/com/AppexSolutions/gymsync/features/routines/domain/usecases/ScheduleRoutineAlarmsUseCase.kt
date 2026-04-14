package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.notifications.RoutineAlarmScheduler
import javax.inject.Inject

class ScheduleRoutineAlarmsUseCase @Inject constructor(
    private val scheduler: RoutineAlarmScheduler
) {
    operator fun invoke(routine: Routine) = scheduler.schedule(routine)
}
