package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.notifications.RoutineAlarmScheduler
import javax.inject.Inject

class ScheduleRoutineAlarmsUseCase @Inject constructor(
    private val scheduler: RoutineAlarmScheduler
) {
    /**
     * @return true si se programó alarma exacta, false si se usó fallback inexacto.
     */
    operator fun invoke(routine: Routine): Boolean = scheduler.schedule(routine)
}
